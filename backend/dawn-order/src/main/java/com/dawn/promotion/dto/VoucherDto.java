package com.dawn.promotion.dto;

import com.dawn.promotion.entity.enums.DiscountType;
import com.dawn.promotion.entity.enums.ScopeType;

import java.math.BigDecimal;
import java.time.Instant;

public record VoucherDto(
        Long id,
        String code,
        ScopeType scopeType,
        Long scopeId,
        DiscountType discountType,
        BigDecimal discountValue,
        BigDecimal maxDiscountAmount,
        BigDecimal minOrderValue,
        int totalQuantity,
        int remainingQuantity,
        Instant validFrom,
        Instant validTo) {
}
