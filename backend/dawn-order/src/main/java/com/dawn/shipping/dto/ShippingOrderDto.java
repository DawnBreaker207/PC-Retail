package com.dawn.shipping.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record ShippingOrderDto(
        Long id,
        Long orderId,
        String trackingCode,
        String carrier,
        String status,
        BigDecimal shippingFee,
        Instant estimatedDelivery,
        Instant createdAt
) {}
