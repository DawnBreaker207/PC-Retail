package com.dawn.order.infra.remote;

import com.dawn.common.contract.StockDecrementCommand;
import com.dawn.common.contract.InventoryClientPort;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
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
    @Bulkhead(name = "inventory")
    public List<Long> decrementBatch(List<StockDecrementCommand> items) {
        return feignClient.decrementBatch(items);
    }

    @Override
    @Bulkhead(name = "inventory")
    public void incrementBatch(List<StockDecrementCommand> items) {
        feignClient.incrementBatch(items);
    }

    @Override
    @Bulkhead(name = "inventory")
    public void reserve(List<StockDecrementCommand> items, Long orderId) {
        feignClient.reserve(items, orderId);
    }

    @Override
    @Bulkhead(name = "inventory")
    public void release(List<StockDecrementCommand> items, Long orderId) {
        feignClient.release(items, orderId);
    }

    @Override
    @Bulkhead(name = "inventory")
    public void deduct(List<StockDecrementCommand> items, Long orderId) {
        feignClient.deduct(items, orderId);
    }

    @Override
    @Bulkhead(name = "inventory")
    public void restock(List<StockDecrementCommand> items, Long actorId) {
        feignClient.restock(items, actorId);
    }
}
