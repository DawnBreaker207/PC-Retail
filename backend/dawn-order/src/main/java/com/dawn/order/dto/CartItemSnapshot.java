package com.dawn.order.dto;

public record CartItemSnapshot(
        Long skuId,
        int quantity) {
}
