package com.dawn.inventory.port.in;

import com.dawn.inventory.dto.StockDecrementCommand;

import java.util.List;

public interface ReserveStockUseCase {
    void reserve(List<StockDecrementCommand> items, Long orderId);
}
