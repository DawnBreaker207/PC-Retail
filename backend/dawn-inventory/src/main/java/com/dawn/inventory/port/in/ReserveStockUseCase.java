package com.dawn.inventory.port.in;

import com.dawn.inventory.dto.StockDecrementCommandDto;

import java.util.List;

public interface ReserveStockUseCase {
    void reserve(List<StockDecrementCommandDto> items, Long orderId);
}
