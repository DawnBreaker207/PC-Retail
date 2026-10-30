package com.dawn.promotion.dto;

import java.math.BigDecimal;

public record VoucherValidationResult(
        boolean valid,
        String errorMessage,
        BigDecimal discountAmount) {
    public static VoucherValidationResult ok(BigDecimal discountAmount) {
        return new VoucherValidationResult(true, null, discountAmount);
    }

    public static VoucherValidationResult fail(String reason) {
        return new VoucherValidationResult(false, reason, BigDecimal.ZERO);
    }
}
