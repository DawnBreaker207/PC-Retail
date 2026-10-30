package com.dawn.order.infra.remote;

import com.dawn.common.contract.StockDecrementCommand;
import com.dawn.common.contract.InventoryClientPort;
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
    public List<Long> decrementBatch(List<StockDecrementCommand> items) {
        return feignClient.decrementBatch(items);
    }

    @Override
    public void incrementBatch(List<StockDecrementCommand> items) {
        feignClient.incrementBatch(items);
    }

    @Override
    public void reserve(List<StockDecrementCommand> items, Long orderId) {
        feignClient.reserve(items, orderId);
    }

    @Override
    public void release(List<StockDecrementCommand> items, Long orderId) {
        feignClient.release(items, orderId);
    }

    @Override
    public void deduct(List<StockDecrementCommand> items, Long orderId) {
        feignClient.deduct(items, orderId);
    }

    @Override
    public void restock(List<StockDecrementCommand> items, Long actorId) {
        feignClient.restock(items, actorId);
    }
}
