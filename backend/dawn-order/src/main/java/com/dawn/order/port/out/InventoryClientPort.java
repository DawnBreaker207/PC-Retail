package com.dawn.order.port.out;

import com.dawn.order.dto.StockDecrementCommand;

import java.util.List;

public interface InventoryClientPort {
    List<Long> decrementBatch(List<StockDecrementCommand> items);

    void incrementBatch(List<StockDecrementCommand> items);

    void reserve(List<StockDecrementCommand> items, Long orderId);

    void release(List<StockDecrementCommand> items, Long orderId);

    void deduct(List<StockDecrementCommand> items, Long orderId);
}
