package com.dawn.common.contract;

public interface InventoryQueryPort {
    Integer getAvailableQuantity(Long skuId);
}
