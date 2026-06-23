package com.dawn.cart.dto;

import java.math.BigDecimal;

public record CartItemDto(
        Long skuId,
        String skuName,
        String primaryImageUrl,
        BigDecimal unitPrice,
        int quantity,
        BigDecimal subtotal) {
}
