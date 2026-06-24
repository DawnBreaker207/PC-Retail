package com.dawn.ai.port.out;

import com.dawn.ai.dto.EmbeddingSearchResult;

import java.util.List;

public interface EmbeddingPort {
    void generateAndSave(Long skuId);

    void generateBatch();

    List<EmbeddingSearchResult> searchSimilar(String query, int limit);
}
