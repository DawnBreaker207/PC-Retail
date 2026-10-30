package com.dawn.rating.dto;

import java.time.Instant;
import java.util.List;

public record RatingDto(
        Long id,
        Long userId,
        Long skuId,
        Integer score,
        String comment,
        List<String> imageUrls,
        String status,
        Instant createdAt
) {}
