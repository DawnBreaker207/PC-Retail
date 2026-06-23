package com.dawn.inventory.dto;

public record StockDecrementCommand(
        Long skuId,
        int quantity) {
}
