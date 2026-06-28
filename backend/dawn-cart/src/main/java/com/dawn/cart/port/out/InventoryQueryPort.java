package com.dawn.cart.port.out;

public interface InventoryQueryPort {
    Integer getAvailableQuantity(Long skuId);
}
