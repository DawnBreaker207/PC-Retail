package com.dawn.order.dto;

import java.math.BigDecimal;

public record OrderItemDto(
        Long skuId,
        String skuName,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal subtotal) {
}
