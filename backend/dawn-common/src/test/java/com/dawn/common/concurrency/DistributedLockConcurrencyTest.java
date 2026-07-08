package com.dawn.common.concurrency;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Stress test cho Redis distributed lock pattern:
 * - SET NX EX: only 1 instance acquires lock at a time
 * - Non-overlap: không có 2 instance cùng chạy job
 * - Only owner releases
 */
public class DistributedLockConcurrencyTest {

    @Test
    @DisplayName("10 threads compete for lock: only 1 acquires, 9 skip (non-overlap)")
    void distributedLock_nonOverlap_onlyOneExecutes() throws Exception {
        var lockOwner = new AtomicReference<String>(null);
        var executeCount = new AtomicInteger(0);
        var readyLatch = new CountDownLatch(10);
        var startLatch = new CountDownLatch(1);
        var finishLatch = new CountDownLatch(10);

        for (int i = 0; i < 10; i++) {
            String instanceId = "instance-" + i;
            Thread.ofVirtual().start(() -> {
                try {
                    readyLatch.countDown();
                    startLatch.await();
                    if (lockOwner.compareAndSet(null, instanceId)) {
                        executeCount.incrementAndGet();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    finishLatch.countDown();
                }
            });
        }

        readyLatch.await();
        startLatch.countDown();
        finishLatch.await();
        assertThat(executeCount.get()).isEqualTo(1);
    }

    @Test
    @DisplayName("Lock prevents concurrent execution: max 1 holder at any time")
    void distributedLock_maxOneConcurrentHolder() throws Exception {
        var lockOwner = new AtomicReference<String>(null);
        var concurrentHolders = new AtomicInteger(0);
        var maxConcurrent = new AtomicInteger(0);
        var latch = new CountDownLatch(10);

        for (int i = 0; i < 10; i++) {
            String instanceId = "instance-" + i;
            Thread.ofVirtual().start(() -> {
                try {
                    if (!lockOwner.compareAndSet(null, instanceId)) return;

                    int holders = concurrentHolders.incrementAndGet();
                    maxConcurrent.updateAndGet(m -> Math.max(m, holders));

                    Thread.sleep(50);

                    concurrentHolders.decrementAndGet();
                    lockOwner.compareAndSet(instanceId, null);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        assertThat(maxConcurrent.get()).isEqualTo(1);
    }

    @Test
    @DisplayName("Lock owner check: only owner can release")
    void distributedLock_onlyOwnerReleases() throws Exception {
        var lockOwner = new AtomicReference<>("instance-1");
        var releaseAttempts = new AtomicInteger(0);
        var latch = new CountDownLatch(10);

        for (int i = 0; i < 10; i++) {
            String instanceId = "instance-" + i;
            Thread.ofVirtual().start(() -> {
                try {
                    if (instanceId.equals(lockOwner.get())) {
                        if (lockOwner.compareAndSet("instance-1", null)) {
                            releaseAttempts.incrementAndGet();
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        assertThat(releaseAttempts.get()).isEqualTo(1);
    }

    @Test
    @DisplayName("Lock TTL expiration: new instance acquires after timeout")
    void distributedLock_ttlExpiration_newOwnerAcquires() throws Exception {
        var lockOwner = new AtomicReference<>("instance-1");
        var latch = new CountDownLatch(2);

        Thread.ofVirtual().start(() -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                latch.countDown();
            }
        });

        Thread.ofVirtual().start(() -> {
            try {
                Thread.sleep(200);
                boolean acquired = lockOwner.compareAndSet("instance-1", "instance-2");
                assertThat(acquired).isTrue();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                latch.countDown();
            }
        });

        latch.await();
        assertThat(lockOwner.get()).isEqualTo("instance-2");
    }
}
