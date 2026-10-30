package com.dawn.common.contract;

public record StockDecrementCommand(
        Long skuId,
        int quantity) {
}
