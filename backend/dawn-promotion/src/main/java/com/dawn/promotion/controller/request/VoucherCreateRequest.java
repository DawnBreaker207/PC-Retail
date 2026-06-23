package com.dawn.promotion.controller.request;

import com.dawn.promotion.entity.enums.DiscountType;
import com.dawn.promotion.entity.enums.ScopeType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;

public record VoucherCreateRequest(
        @NotBlank String code,
        @NotNull ScopeType scopeType,
        Long scopeId,
        @NotNull DiscountType discountType,
        @NotNull @DecimalMin("0.01") BigDecimal discountValue,
        BigDecimal maxDiscountAmount,
        BigDecimal minOrderValue,
        @NotNull @Min(1) Integer totalQuantity,
        @NotNull Instant validFrom,
        @NotNull Instant validTo) {
}
