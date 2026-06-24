package com.dawn.inventory.port.out;

import com.dawn.inventory.entity.Inventory;

import java.util.List;
import java.util.Optional;

public interface InventoryRepository {
    Optional<Inventory> findBySkuId(Long skuId);

    Optional<Inventory> findBySkuIdWithLock(Long skuId);

    List<Inventory> findLowStock(int threshold);
}
