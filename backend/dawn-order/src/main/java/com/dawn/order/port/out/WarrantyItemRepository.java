package com.dawn.order.port.out;

import com.dawn.order.entity.WarrantyItem;

import java.util.List;
import java.util.Optional;

public interface WarrantyItemRepository {
    WarrantyItem save(WarrantyItem warrantyItem);
    Optional<WarrantyItem> findById(Long id);
    List<WarrantyItem> findBySubOrderId(Long subOrderId);
    List<WarrantyItem> findBySkuId(Long skuId);
}
