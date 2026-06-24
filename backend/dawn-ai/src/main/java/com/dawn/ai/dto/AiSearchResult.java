package com.dawn.ai.dto;

import java.math.BigDecimal;
import java.util.List;

public record AiSearchResult(
        Long skuId,
        String name,
        String categoryName,
        String brandName,
        BigDecimal price,
        String thumbnailUrl,
        double similarityScore) {
}
