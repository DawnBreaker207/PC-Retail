package com.dawn.order.infra.remote;

import com.dawn.common.contract.InventoryClientPort;
import com.dawn.common.contract.StockDecrementCommand;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.services.mode", havingValue = "distributed")
public class InventoryRemoteAdapter implements InventoryClientPort {

    private final InventoryFeignClient feignClient;

    @Override
    @CircuitBreaker(name = "inventory", fallbackMethod = "decrementBatchFallback")
    @Bulkhead(name = "inventory")
    public List<Long> decrementBatch(List<StockDecrementCommand> items) {
        return feignClient.decrementBatch(items);
    }

    @Override
    @CircuitBreaker(name = "inventory", fallbackMethod = "incrementBatchFallback")
    @Bulkhead(name = "inventory")
    public void incrementBatch(List<StockDecrementCommand> items) {
        feignClient.incrementBatch(items);
    }

    @Override
    @CircuitBreaker(name = "inventory", fallbackMethod = "reserveFallback")
    @Bulkhead(name = "inventory")
    public void reserve(List<StockDecrementCommand> items, Long orderId) {
        feignClient.reserve(items, orderId);
    }

    @Override
    @CircuitBreaker(name = "inventory", fallbackMethod = "releaseFallback")
    @Bulkhead(name = "inventory")
    public void release(List<StockDecrementCommand> items, Long orderId) {
        feignClient.release(items, orderId);
    }

    @Override
    @CircuitBreaker(name = "inventory", fallbackMethod = "deductFallback")
    @Bulkhead(name = "inventory")
    public void deduct(List<StockDecrementCommand> items, Long orderId) {
        feignClient.deduct(items, orderId);
    }

    @Override
    @CircuitBreaker(name = "inventory", fallbackMethod = "restockFallback")
    @Bulkhead(name = "inventory")
    public void restock(List<StockDecrementCommand> items, Long actorId) {
        feignClient.restock(items, actorId);
    }

    @Override
    @CircuitBreaker(name = "inventory", fallbackMethod = "routeWarehouseFallback")
    @Bulkhead(name = "inventory")
    public Long routeWarehouse(List<StockDecrementCommand> items) {
        return feignClient.routeWarehouse(items);
    }

    // --- fallbacks: all throw to let callers (SagaSweep, CheckoutService) handle the error ---

    List<Long> decrementBatchFallback(List<StockDecrementCommand> items, Throwable t) {
        log.error("Inventory circuit OPEN — decrementBatch failed: {}", t.getMessage());
        throw new RuntimeException("Inventory service unavailable: " + t.getMessage(), t);
    }

    void incrementBatchFallback(List<StockDecrementCommand> items, Throwable t) {
        log.warn("Inventory circuit OPEN — incrementBatch failed: {}", t.getMessage());
        throw new RuntimeException("Inventory service unavailable: " + t.getMessage(), t);
    }

    void reserveFallback(List<StockDecrementCommand> items, Long orderId, Throwable t) {
        log.error("Inventory circuit OPEN — reserve failed for order={}: {}", orderId, t.getMessage());
        throw new RuntimeException("Inventory service unavailable: " + t.getMessage(), t);
    }

    void releaseFallback(List<StockDecrementCommand> items, Long orderId, Throwable t) {
        log.warn("Inventory circuit OPEN — release failed for order={}: {}", orderId, t.getMessage());
        throw new RuntimeException("Inventory service unavailable: " + t.getMessage(), t);
    }

    void deductFallback(List<StockDecrementCommand> items, Long orderId, Throwable t) {
        log.error("Inventory circuit OPEN — deduct failed for order={}: {}", orderId, t.getMessage());
        throw new RuntimeException("Inventory service unavailable: " + t.getMessage(), t);
    }

    void restockFallback(List<StockDecrementCommand> items, Long actorId, Throwable t) {
        log.warn("Inventory circuit OPEN — restock failed actorId={}: {}", actorId, t.getMessage());
        throw new RuntimeException("Inventory service unavailable: " + t.getMessage(), t);
    }

    Long routeWarehouseFallback(List<StockDecrementCommand> items, Throwable t) {
        log.error("Inventory circuit OPEN — routeWarehouse failed: {}", t.getMessage());
        throw new RuntimeException("Inventory service unavailable: " + t.getMessage(), t);
    }
}
