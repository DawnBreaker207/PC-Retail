package com.dawn.tax.dto;

import java.math.BigDecimal;
import java.util.Map;

public record TaxCalculateResponse(
        BigDecimal subtotal,
        BigDecimal taxAmount,
        BigDecimal total,
        BigDecimal taxRate,
        Map<String, BigDecimal> taxBreakdown
) {}
