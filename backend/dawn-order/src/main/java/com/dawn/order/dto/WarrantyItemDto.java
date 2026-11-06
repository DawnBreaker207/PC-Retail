package com.dawn.order.dto;

import java.time.Instant;

public record WarrantyItemDto(
        Long id,
        Long orderItemId,
        Long subOrderId,
        Long skuId,
        int shopMonths,
        int mfrMonths,
        Instant activatedAt,
        Instant expiresAt,
        String serialNumber,
        boolean active
) {}
