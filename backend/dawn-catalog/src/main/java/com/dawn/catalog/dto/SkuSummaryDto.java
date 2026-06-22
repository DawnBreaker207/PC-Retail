package com.dawn.catalog.dto;

import com.dawn.catalog.entity.enums.SkuStatus;

import java.math.BigDecimal;

public record SkuSummaryDto(
        Long id,
        String skuCode,
        String name,
        String slug,
        BigDecimal price,
        SkuStatus status,
        String primaryImageUrl,
        Long brandId,
        Long categoryId) {
}
