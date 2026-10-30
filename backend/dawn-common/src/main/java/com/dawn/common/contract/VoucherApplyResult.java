package com.dawn.common.contract;

import java.math.BigDecimal;

public record VoucherApplyResult(
        boolean valid,
        BigDecimal discountAmount,
        String errorMessage) {
    public static VoucherApplyResult ok(BigDecimal discount) {
        return new VoucherApplyResult(true, discount, null);
    }

    public static VoucherApplyResult fail(String message) {
        return new VoucherApplyResult(false, BigDecimal.ZERO, message);
    }
}
