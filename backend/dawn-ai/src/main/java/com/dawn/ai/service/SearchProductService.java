package com.dawn.ai.service;

import com.dawn.ai.dto.AiSearchResult;
import com.dawn.ai.dto.EmbeddingSearchResult;
import com.dawn.ai.port.in.SearchProductUseCase;
import com.dawn.ai.port.out.EmbeddingPort;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchProductService implements SearchProductUseCase {
    private final EmbeddingPort embeddingPort;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<AiSearchResult> search(String query, int limit) {
        // Bước 1: Semantic search → lấy list (skuId, similarityScore)
        List<EmbeddingSearchResult> similar = embeddingPort.searchSimilar(query, limit);

        if (similar.isEmpty()) return List.of();

        // Bước 2: Map skuId → similarity score
        Map<Long, Double> scoreMap = similar.stream()
                .collect(Collectors.toMap(EmbeddingSearchResult::skuId, EmbeddingSearchResult::similarity));

        // Bước 3: Query SKU details
        String inClause = scoreMap.keySet().stream().map(String::valueOf).collect(Collectors.joining(","));
        String sql = String.format("""
                SELECT s.id, s.name, c.name as category_name, b.name as brand_name,
                       s.price, s.thumbnail_url
                FROM sku s
                LEFT JOIN category c ON c.id = s.category_id
                LEFT JOIN brand b ON b.id = s.brand_id
                WHERE s.id IN (%s) AND s.deleted_at IS NULL
                """, inClause);

        List<AiSearchResult> results = jdbcTemplate.query(sql, (rs, rowNum) -> new AiSearchResult(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getString("category_name"),
                rs.getString("brand_name"),
                rs.getBigDecimal("price"),
                rs.getString("thumbnail_url"),
                scoreMap.getOrDefault(rs.getLong("id"), 0.0)
        ));

        // Bước 4: Sort lại theo similarity score (query DB không giữ thứ tự IN clause)
        results.sort((a, b) -> Double.compare(b.similarityScore(), a.similarityScore()));
        return results;
    }
}
