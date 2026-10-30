package com.dawn.shipping.dto;

import java.math.BigDecimal;

public record ShippingFeeResponse(String carrier, BigDecimal fee, Integer estimatedDays) {}
