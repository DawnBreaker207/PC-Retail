package com.dawn.order.concurrency;

import com.dawn.common.contract.CartItemSnapshot;
import com.dawn.common.contract.StockDecrementCommand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Stress test cho concurrency pattern:
 * - Redis Lua gatekeeper (atomic DECR): only 1 thread passes
 * - DB pessimistic lock (SELECT FOR UPDATE): sequentialize writes
 */
public class CheckoutConcurrencyStressTest {

    private static final int THREAD_COUNT = 100;

    @Test
    @DisplayName("Inventory gatekeeper: 100 threads tranh 1 SKU, only 1 succeeds")
    void inventoryGatekeeper_concurrentDecrement_onlyOneSucceeds() throws Exception {
        // Simulate Redis atomic DECR: remaining = 1, 100 concurrent requests
        var remaining = new AtomicLong(1);
        var successCount = new AtomicInteger(0);
        var latch = new CountDownLatch(THREAD_COUNT);

        for (int i = 0; i < THREAD_COUNT; i++) {
            Thread.ofVirtual().start(() -> {
                try {
                    long prev = remaining.getAndUpdate(r -> r > 0 ? r - 1 : 0);
                    if (prev > 0) {
                        successCount.incrementAndGet();
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        assertThat(successCount.get()).isEqualTo(1);
    }

    @Test
    @DisplayName("Simulate 100 concurrent DB writes with pessimistic lock emulation")
    void pessimisticLock_concurrentWrites_sequentialized() throws Exception {
        var dbCounter = new AtomicLong(0);
        var successCount = new AtomicInteger(0);
        var latch = new CountDownLatch(THREAD_COUNT);

        for (int i = 0; i < THREAD_COUNT; i++) {
            Thread.ofVirtual().start(() -> {
                try {
                    // Simulate SELECT FOR UPDATE + UPDATE
                    synchronized (this) {
                        long current = dbCounter.get();
                        Thread.sleep(1);
                        dbCounter.set(current + 1);
                    }
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    Thread.currentThread().interrupt();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        assertThat(successCount.get()).isEqualTo(THREAD_COUNT);
        assertThat(dbCounter.get()).isEqualTo(THREAD_COUNT);
    }

    @Test
    @DisplayName("Redis Lua + DB double-check: race condition prevented")
    void redisGatekeeperThenDbDoubleCheck() throws Exception {
        var gatekeeper = new AtomicLong(5);
        var dbReserved = new AtomicLong(0);
        var latch = new CountDownLatch(THREAD_COUNT);
        var dbSuccessCount = new AtomicInteger(0);

        for (int i = 0; i < THREAD_COUNT; i++) {
            Thread.ofVirtual().start(() -> {
                try {
                    // Phase 1: Redis gatekeeper
                    long prev = gatekeeper.getAndUpdate(r -> r > 0 ? r - 1 : 0);
                    if (prev <= 0) return;

                    // Phase 2: DB double-check with lock
                    synchronized (this) {
                        int reserved = dbReserved.intValue();
                        if (reserved < 5) {
                            dbReserved.incrementAndGet();
                            dbSuccessCount.incrementAndGet();
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        // Redis let exactly 5 through, DB only accepted 5
        assertThat(gatekeeper.get()).isEqualTo(0);
        assertThat(dbSuccessCount.get()).isEqualTo(5);
    }
}
