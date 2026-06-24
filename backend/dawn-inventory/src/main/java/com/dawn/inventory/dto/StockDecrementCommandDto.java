package com.dawn.inventory.dto;

public record StockDecrementCommandDto(
        Long skuId,
        int quantity) {
}
