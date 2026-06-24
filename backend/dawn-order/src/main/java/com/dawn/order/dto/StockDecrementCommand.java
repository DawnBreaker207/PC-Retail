package com.dawn.order.dto;

public record StockDecrementCommand(
        Long skuId,
        int quantity) {
}
