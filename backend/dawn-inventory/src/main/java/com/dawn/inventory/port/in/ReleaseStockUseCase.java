package com.dawn.inventory.port.in;

import com.dawn.inventory.dto.StockDecrementCommand;

import java.util.List;

public interface ReleaseStockUseCase {
    void release(List<StockDecrementCommand> items, Long orderId);
}
