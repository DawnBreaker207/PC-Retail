package com.dawn.promotion.port.in;

import com.dawn.promotion.dto.VoucherValidationResult;

import java.math.BigDecimal;

public interface ValidatePromotionUseCase {
    VoucherValidationResult validate(String code, BigDecimal orderTotal);
}
