package com.dawn.inventory.port.in;

import com.dawn.inventory.dto.StockDecrementCommand;

import java.util.List;

public interface DeductStockUseCase {
    void deduct(List<StockDecrementCommand> items, Long orderId);
}
