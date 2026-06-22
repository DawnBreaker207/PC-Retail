package com.dawn.catalog.controller.request;

import com.dawn.catalog.entity.enums.ProductType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record SkuCreatedRequest(
        @NotBlank String name,
        @NotBlank String skuCode,
        String description,
        Long brandId,
        @NotNull Long categoryId,
        @NotNull @DecimalMin("0.0") BigDecimal price,
        ProductType productType,
        Map<String, Object> attributes,
        Map<String, Object> specs,
        List<String> imageUrls) {
}
