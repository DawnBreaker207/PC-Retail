package com.dawn.inventory.port.out;

import com.dawn.inventory.dto.StockDecrementCommandDto;

import java.util.List;

public interface InventoryCachePort {
    void setStock(Long warehouseId, Long skuId, int quantity);

    List<Long> decrementBatch(List<StockDecrementCommandDto> items);

    void incrementBatch(List<StockDecrementCommandDto> items);

    void syncFromDb(Long warehouseId, Long skuId, int availableQuantity);

    Integer getAvailable(Long warehouseId, Long skuId);
}
