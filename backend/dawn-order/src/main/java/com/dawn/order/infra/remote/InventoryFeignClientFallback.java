package com.dawn.order.infra.remote;

import com.dawn.common.contract.StockDecrementCommand;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class InventoryFeignClientFallback implements InventoryFeignClient {

    @Override
    public List<Long> decrementBatch(List<StockDecrementCommand> items) {
        log.warn("Inventory service unavailable — decrement batch failed for {} items", items.size());
        return List.of();
    }

    @Override
    public void incrementBatch(List<StockDecrementCommand> items) {
        log.warn("Inventory service unavailable — increment batch skipped for {} items", items.size());
    }

    @Override
    public void reserve(List<StockDecrementCommand> items, Long orderId) {
        log.warn("Inventory service unavailable — reserve skipped for orderId={}", orderId);
    }

    @Override
    public void release(List<StockDecrementCommand> items, Long orderId) {
        log.warn("Inventory service unavailable — release skipped for orderId={}", orderId);
    }

    @Override
    public void deduct(List<StockDecrementCommand> items, Long orderId) {
        log.warn("Inventory service unavailable — deduct skipped for orderId={}", orderId);
    }

    @Override
    public void restock(List<StockDecrementCommand> items, Long actorId) {
        log.warn("Inventory service unavailable — restock skipped");
    }

    @Override
    public Integer available(Long skuId) {
        log.warn("Inventory service unavailable — returning 0 available for skuId={}", skuId);
        return 0;
    }

    @Override
    public Long routeWarehouse(List<StockDecrementCommand> items) {
        log.warn("Inventory service unavailable — warehouse routing failed");
        return null;
    }
}
