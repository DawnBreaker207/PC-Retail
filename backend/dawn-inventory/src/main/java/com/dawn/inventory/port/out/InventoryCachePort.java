package com.dawn.inventory.port.out;

import com.dawn.inventory.dto.StockDecrementCommandDto;

import java.util.List;

public interface InventoryCachePort {
    void setStock(Long skuId, int quantity);

    List<Long> decrementBatch(List<StockDecrementCommandDto> items);

    void incrementBatch(List<StockDecrementCommandDto> items);

    void syncFromDb(Long skuId, int availableQuantity);

    Integer getAvailable(Long skuId);
}
