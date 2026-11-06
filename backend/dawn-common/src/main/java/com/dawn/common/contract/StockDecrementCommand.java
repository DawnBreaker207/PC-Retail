package com.dawn.common.contract;

public record StockDecrementCommand(
        Long skuId,
        int quantity,
        Long warehouseId) {

    public StockDecrementCommand(Long skuId, int quantity) {
        this(skuId, quantity, null);
    }
}
