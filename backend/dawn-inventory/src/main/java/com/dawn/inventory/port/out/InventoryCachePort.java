package com.dawn.inventory.port.out;

import com.dawn.inventory.dto.StockDecrementCommand;

import java.util.List;

public interface InventoryCachePort {
    void setStock(Long skuId, int quantity);

    List<Long> decrementBatch(List<StockDecrementCommand> items);

    void incrementBatch(List<StockDecrementCommand> items);

    void syncFromDb(Long skuId, int availableQuantity);

    Integer getAvailable(Long skuId);
}
