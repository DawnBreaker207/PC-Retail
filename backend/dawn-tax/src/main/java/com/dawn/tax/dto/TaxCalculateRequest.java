package com.dawn.tax.dto;

import com.dawn.tax.entity.enums.BuyerType;

import java.util.List;

public record TaxCalculateRequest(
        Long orderId,
        Long userId,
        BuyerType buyerType,
        String businessTaxCode,
        List<TaxLineItem> items
) {}
