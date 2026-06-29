package com.dawn.inventory.port.out;

import com.dawn.inventory.entity.Inventory;
import com.dawn.inventory.entity.InventoryTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface InventoryTransactionRepository {
    Page<InventoryTransaction> findBySkuId(Long skuId, Pageable pageable);

    InventoryTransaction save(InventoryTransaction build);
}
