package com.dawn.ai.dto;

public record EmbeddingSearchResult(
        Long skuId,
        double similarity
) {
}
