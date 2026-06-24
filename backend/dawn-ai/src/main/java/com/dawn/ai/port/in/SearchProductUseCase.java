package com.dawn.ai.port.in;

import com.dawn.ai.dto.AiSearchResult;

import java.util.List;

public interface SearchProductUseCase {
    List<AiSearchResult> search(String query, int limit);
}
