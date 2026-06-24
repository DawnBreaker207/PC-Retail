package com.dawn.ai.dto;

import java.math.BigDecimal;

public record SkuSummaryDto(
        Long skuId,
        String name,
        BigDecimal price,
        int stockQuantity,
        String specs) {
}
