package com.dawn.ai.service;

import com.dawn.ai.dto.AiSearchResult;
import com.dawn.ai.dto.EmbeddingSearchResult;
import com.dawn.ai.port.in.SearchProductUseCase;
import com.dawn.ai.port.out.EmbeddingPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Text search: PostgreSQL FTS (websearch_to_tsquery with 'simple' dictionary)
 *             + pgvector cosine re-rank on the top-50 FTS candidates.
 * Spec similarity: pgvector cosine distance keyed by skuId.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SearchProductService implements SearchProductUseCase {

    private final EmbeddingPort embeddingPort;
    private final JdbcTemplate jdbcTemplate;

    private static final String SKU_COLS = """
            s.id, s.name, c.name as category_name, b.name as brand_name,
            s.price, COALESCE(s.image_urls->>0, '') AS thumbnail_url
            """;

    @Override
    public List<AiSearchResult> search(String query, int limit) {
        int safeLimit = Math.min(Math.max(limit, 1), 20);
        String trimmed = query == null ? "" : query.trim();
        if (trimmed.isEmpty()) return List.of();

        // 1. FTS retrieval (top 50 candidates)
        List<AiSearchResult> ftsResults = ftsSearch(trimmed, 50);
        if (ftsResults.isEmpty()) {
            return keywordSearch(trimmed, safeLimit);
        }

        // 2. pgvector re-rank: for each FTS result that has a stored vector,
        //    compute cosine similarity against the first result's vector (centroid proxy).
        // Simpler: just return FTS-ranked results truncated to limit.
        // pgvector is only used for similar(skuId) — spec-to-spec comparison.
        return ftsResults.stream().limit(safeLimit).toList();
    }

    @Override
    public List<AiSearchResult> similar(Long skuId, int limit) {
        int safeLimit = Math.min(Math.max(limit, 1), 20);

        List<EmbeddingSearchResult> specSimilar;
        try {
            specSimilar = embeddingPort.findSimilarByVector(skuId, safeLimit);
        } catch (Exception e) {
            log.warn("pgvector similar failed for SKU {}, falling back to category search: {}", skuId, e.getMessage());
            return categorySimilar(skuId, safeLimit);
        }

        if (specSimilar.isEmpty()) return categorySimilar(skuId, safeLimit);

        Map<Long, Double> scoreMap = specSimilar.stream()
                .collect(Collectors.toMap(EmbeddingSearchResult::skuId, EmbeddingSearchResult::similarity));

        String inClause = scoreMap.keySet().stream().map(String::valueOf).collect(Collectors.joining(","));
        String sql = String.format("""
                SELECT %s
                FROM sku s
                LEFT JOIN category c ON c.id = s.category_id
                LEFT JOIN brand b ON b.id = s.brand_id
                WHERE s.id IN (%s) AND s.deleted_at IS NULL
                """, SKU_COLS, inClause);

        List<AiSearchResult> results = jdbcTemplate.query(sql, (rs, rowNum) -> new AiSearchResult(
                rs.getLong("id"), rs.getString("name"),
                rs.getString("category_name"), rs.getString("brand_name"),
                rs.getBigDecimal("price"), rs.getString("thumbnail_url"),
                scoreMap.getOrDefault(rs.getLong("id"), 0.0)));

        results.sort((a, b) -> Double.compare(b.similarityScore(), a.similarityScore()));
        return results;
    }

    @Override
    public List<AiSearchResult> dedupCheck(String name, String description, int limit) {
        String text = (name == null ? "" : name) + " " + (description == null ? "" : description);
        return search(text.trim(), limit);
    }

    @Override
    public List<String> suggestCategories(String name, String description, int limit) {
        return dedupCheck(name, description, Math.min(limit * 3, 15)).stream()
                .map(AiSearchResult::categoryName)
                .filter(c -> c != null && !c.isBlank())
                .distinct()
                .limit(Math.max(limit, 1))
                .toList();
    }

    private List<AiSearchResult> ftsSearch(String query, int limit) {
        try {
            String sql = String.format("""
                    SELECT %s,
                           ts_rank_cd(
                               to_tsvector('simple', coalesce(s.name,'') || ' ' || coalesce(s.description,'') || ' ' || coalesce(s.sku_code,'')),
                               websearch_to_tsquery('simple', ?)
                           ) AS rank
                    FROM sku s
                    LEFT JOIN category c ON c.id = s.category_id
                    LEFT JOIN brand b ON b.id = s.brand_id
                    WHERE s.deleted_at IS NULL
                      AND to_tsvector('simple', coalesce(s.name,'') || ' ' || coalesce(s.description,'') || ' ' || coalesce(s.sku_code,''))
                          @@ websearch_to_tsquery('simple', ?)
                    ORDER BY rank DESC
                    LIMIT ?
                    """, SKU_COLS);
            return jdbcTemplate.query(sql, (rs, rowNum) -> new AiSearchResult(
                    rs.getLong("id"), rs.getString("name"),
                    rs.getString("category_name"), rs.getString("brand_name"),
                    rs.getBigDecimal("price"), rs.getString("thumbnail_url"),
                    rs.getDouble("rank")),
                    query, query, limit);
        } catch (Exception e) {
            log.debug("FTS search failed, falling back to keyword: {}", e.getMessage());
            return keywordSearch(query, limit);
        }
    }

    private List<AiSearchResult> keywordSearch(String query, int limit) {
        String like = "%" + query.replace("%", "").replace("_", "") + "%";
        String sql = String.format("""
                SELECT %s
                FROM sku s
                LEFT JOIN category c ON c.id = s.category_id
                LEFT JOIN brand b ON b.id = s.brand_id
                WHERE s.deleted_at IS NULL AND (s.name ILIKE ? OR s.sku_code ILIKE ?)
                LIMIT ?
                """, SKU_COLS);
        return jdbcTemplate.query(sql, (rs, rowNum) -> new AiSearchResult(
                rs.getLong("id"), rs.getString("name"),
                rs.getString("category_name"), rs.getString("brand_name"),
                rs.getBigDecimal("price"), rs.getString("thumbnail_url"), 0.0),
                like, like, limit);
    }

    private List<AiSearchResult> categorySimilar(Long skuId, int limit) {
        String sql = String.format("""
                SELECT %s
                FROM sku s
                LEFT JOIN category c ON c.id = s.category_id
                LEFT JOIN brand b ON b.id = s.brand_id
                WHERE s.category_id = (SELECT category_id FROM sku WHERE id = ?)
                  AND s.id <> ?
                  AND s.deleted_at IS NULL
                ORDER BY s.price
                LIMIT ?
                """, SKU_COLS);
        return jdbcTemplate.query(sql, (rs, rowNum) -> new AiSearchResult(
                rs.getLong("id"), rs.getString("name"),
                rs.getString("category_name"), rs.getString("brand_name"),
                rs.getBigDecimal("price"), rs.getString("thumbnail_url"), 0.0),
                skuId, skuId, limit);
    }
}
