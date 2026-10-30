package com.dawn.tax.dto;

import java.math.BigDecimal;

public record TaxLineItem(
        Long skuId,
        String productType,
        Integer quantity,
        BigDecimal unitPrice
) {}
