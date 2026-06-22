package com.dawn.catalog.dto;

import com.dawn.catalog.entity.enums.ProductType;
import com.dawn.catalog.entity.enums.SkuStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record SkuDetailDto(
        Long id,
        String skuCode,
        String name,
        String slug,
        String description,
        BigDecimal price,
        SkuStatus status,
        ProductType productType,
        Map<String, Object> attributes,
        Map<String, Object> specs,
        List<String> imageUrls,
        Long brandId,
        Long categoryId,
        Integer stockQuantity) {
}
