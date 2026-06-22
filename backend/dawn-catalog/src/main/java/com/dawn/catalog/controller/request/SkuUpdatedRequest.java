package com.dawn.catalog.controller.request;

import com.dawn.catalog.entity.enums.SkuStatus;
import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record SkuUpdatedRequest(
        String name,
        String description,
        @DecimalMin("0.0") BigDecimal price,
        SkuStatus status,
        Map<String, Object> attributes,
        Map<String, Object> specs,
        List<String> imageUrls) {
}
