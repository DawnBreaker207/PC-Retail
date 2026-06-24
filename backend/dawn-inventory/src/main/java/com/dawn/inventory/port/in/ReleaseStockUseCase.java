package com.dawn.inventory.port.in;

import com.dawn.inventory.dto.StockDecrementCommandDto;

import java.util.List;

public interface ReleaseStockUseCase {
    void release(List<StockDecrementCommandDto> items, Long orderId);
}
