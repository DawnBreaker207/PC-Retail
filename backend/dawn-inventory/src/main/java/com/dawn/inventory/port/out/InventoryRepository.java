package com.dawn.inventory.port.out;

import com.dawn.inventory.entity.Inventory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface InventoryRepository {
    Optional<Inventory> findBySkuId(Long skuId);

    Optional<Inventory> findBySkuIdAndWarehouseIdWithLock(Long skuId, Long warehouseId);

    List<Inventory> findLowStock(int threshold);

    List<Inventory> findBySkuIdIn(List<Long> skuIds);

    Inventory save(Inventory inv);

    Page<Inventory> findFiltered(Long skuId, boolean lowStock, int lowStockThreshold, Pageable pageable);
}
