package com.dawn.shipping.dto;

public record ShippingFeeRequest(
        String fromAddress,
        String toAddress,
        Integer weightGrams,
        String carrier
) {}
