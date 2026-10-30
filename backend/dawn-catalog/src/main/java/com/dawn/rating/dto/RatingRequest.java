package com.dawn.rating.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record RatingRequest(
        @NotNull Long skuId,
        @NotNull Long orderId,
        @NotNull @Min(1) @Max(5) Integer score,
        @Size(max = 2000) String comment,
        List<String> imageUrls
) {}
