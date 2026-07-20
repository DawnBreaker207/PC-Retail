package com.dawn.engagement.dto;

import java.util.List;

public record ReviewDto(
        Long id,
        Long subOrderId,
        Long skuId,
        Long userId,
        int rating,
        String comment,
        List<String> images) {
}
