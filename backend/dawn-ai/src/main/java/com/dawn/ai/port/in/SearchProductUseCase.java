package com.dawn.ai.port.in;

import com.dawn.ai.dto.AiSearchResult;

import java.util.List;

public interface SearchProductUseCase {
    List<AiSearchResult> search(String query, int limit);

    /** Related products: vector-to-vector, loại chính nó. */
    List<AiSearchResult> similar(Long skuId, int limit);

    /** Admin dedup-check: SKU đã có nào gần nhất với tên/mô tả định tạo. */
    List<AiSearchResult> dedupCheck(String name, String description, int limit);

    /** Category auto-suggestion: category của K SKU gần nhất. */
    List<String> suggestCategories(String name, String description, int limit);
}
