package com.dawn.cart.dto;

import java.math.BigDecimal;
import java.util.List;

public record CartSummaryDto(
        List<CartItemDto> items,
        int totalItems,
        BigDecimal totalAmount) {
}
