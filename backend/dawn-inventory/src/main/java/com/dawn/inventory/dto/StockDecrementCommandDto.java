package com.dawn.inventory.dto;

public record StockDecrementCommandDto(
        Long skuId,
        int quantity,
        Long warehouseId) {

    public StockDecrementCommandDto(Long skuId, int quantity) {
        this(skuId, quantity, null);
    }
}
