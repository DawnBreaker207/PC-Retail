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
        List<EmbeddingSearchResult> similar;
        try {
            similar = embeddingPort.searchSimilar(query, limit);
        } catch (Exception e) {
            // no AI key or embedding error — fall back to keyword search instead of 500
            org.slf4j.LoggerFactory.getLogger(SearchProductService.class)
                    .warn("Semantic search failed, fallback to keyword: {}", e.toString());
            return keywordSearch(query, limit);
        }

        if (similar.isEmpty()) return keywordSearch(query, limit);

        Map<Long, Double> scoreMap = similar.stream()
                .collect(Collectors.toMap(EmbeddingSearchResult::skuId, EmbeddingSearchResult::similarity));

        String inClause = scoreMap.keySet().stream().map(String::valueOf).collect(Collectors.joining(","));
        String sql = String.format("""
                SELECT s.id, s.name, c.name as category_name, b.name as brand_name,
                       s.price, COALESCE(s.image_urls->>0, '') AS thumbnail_url
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

        // re-sort by similarity score; DB does not preserve IN-clause order
        results.sort((a, b) -> Double.compare(b.similarityScore(), a.similarityScore()));
        return results;
    }

    @Override
    public List<AiSearchResult> dedupCheck(String name, String description, int limit) {
        String text = (name == null ? "" : name) + " " + (description == null ? "" : description);
        return search(text.trim(), limit);
    }

    @Override
    public List<AiSearchResult> similar(Long skuId, int limit) {
        int safeLimit = Math.min(Math.max(limit, 1), 10);
        // find K nearest neighbors of the source SKU's vector, excluding itself
        String sql = """
                SELECT s.id, s.name, c.name as category_name, b.name as brand_name,
                       s.price, COALESCE(s.image_urls->>0, '') AS thumbnail_url,
                       1 - (pe.embedding <=> (SELECT embedding FROM product_embedding WHERE sku_id = ?)) AS similarity
                FROM product_embedding pe
                JOIN sku s ON s.id = pe.sku_id AND s.deleted_at IS NULL
                LEFT JOIN category c ON c.id = s.category_id
                LEFT JOIN brand b ON b.id = s.brand_id
                WHERE pe.sku_id <> ?
                ORDER BY pe.embedding <=> (SELECT embedding FROM product_embedding WHERE sku_id = ?)
                LIMIT ?
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> new AiSearchResult(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getString("category_name"),
                rs.getString("brand_name"),
                rs.getBigDecimal("price"),
                rs.getString("thumbnail_url"),
                rs.getDouble("similarity")),
                skuId, skuId, skuId, safeLimit);
    }

    @Override
    public List<String> suggestCategories(String name, String description, int limit) {
        return dedupCheck(name, description, Math.min(Math.max(limit * 3, 3), 15)).stream()
                .map(AiSearchResult::categoryName)
                .filter(c -> c != null && !c.isBlank())
                .distinct()
                .limit(Math.max(limit, 1))
                .toList();
    }

    /** Fallback khi không có embedding: ILIKE tên + mã SKU. */
    private List<AiSearchResult> keywordSearch(String query, int limit) {
        int safeLimit = Math.min(Math.max(limit, 1), 10);
        String like = "%" + query.trim().replace("%", "").replace("_", "") + "%";
        String sql = """
                SELECT s.id, s.name, c.name as category_name, b.name as brand_name,
                       s.price, COALESCE(s.image_urls->>0, '') AS thumbnail_url
                FROM sku s
                LEFT JOIN category c ON c.id = s.category_id
                LEFT JOIN brand b ON b.id = s.brand_id
                WHERE s.deleted_at IS NULL AND (s.name ILIKE ? OR s.sku_code ILIKE ?)
                LIMIT ?
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> new AiSearchResult(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getString("category_name"),
                rs.getString("brand_name"),
                rs.getBigDecimal("price"),
                rs.getString("thumbnail_url"),
                0.0),
                like, like, safeLimit);
    }
}
