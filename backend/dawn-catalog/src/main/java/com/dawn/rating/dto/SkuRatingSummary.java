package com.dawn.rating.dto;

import java.util.Map;

public record SkuRatingSummary(
        Long skuId,
        Double averageScore,
        Long totalCount,
        Map<Integer, Long> distribution
) {}
