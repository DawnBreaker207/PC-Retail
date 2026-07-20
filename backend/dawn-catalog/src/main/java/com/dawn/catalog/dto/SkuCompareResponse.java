package com.dawn.catalog.dto;

import java.math.BigDecimal;
import java.util.Map;

public record SkuCompareResponse(
        Long id,
        String name,
        String slug,
        BigDecimal price,
        String imageUrl,
        Map<String, Object> specs) {
}
