package com.dawn.inventory.dto;

public interface InventoryQueryPort {
    Integer getAvailableQuantity(Long skuId);
}
