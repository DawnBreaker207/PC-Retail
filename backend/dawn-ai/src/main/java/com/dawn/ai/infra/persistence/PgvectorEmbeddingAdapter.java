package com.dawn.ai.infra.persistence;

import com.dawn.ai.dto.EmbeddingSearchResult;
import com.dawn.ai.dto.SkuEmbeddingSourceDto;
import com.dawn.ai.entity.ProductEmbedding;
import com.dawn.ai.port.out.EmbeddingPort;
import com.dawn.ai.vectorizer.ProductSpecVectorizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class PgvectorEmbeddingAdapter implements EmbeddingPort {

    private final JpaProductEmbeddingRepository repository;
    private final ProductSpecVectorizer vectorizer;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void generateAndSave(Long skuId) {
        SkuEmbeddingSourceDto source = fetchSkuSource(skuId);
        long price = fetchPrice(skuId);

        float[] vector = vectorizer.vectorize(skuId, price, source.categoryName(), source.specs());

        ProductEmbedding pe = repository.findBySkuId(skuId)
                .orElseGet(ProductEmbedding::new);
        pe.setSkuId(skuId);
        pe.setEmbedding(vector);
        pe.setEmbeddingText(source.toEmbeddingText());
        pe.setModelVersion("spec-v1");
        repository.save(pe);

        log.debug("Saved spec vector for SKU {}", skuId);
    }

    @Override
    public void generateBatch() {
        List<Long> skuIds = jdbcTemplate.queryForList(
                "SELECT id FROM sku WHERE deleted_at IS NULL", Long.class);
        int ok = 0, fail = 0;
        for (Long skuId : skuIds) {
            try {
                generateAndSave(skuId);
                ok++;
            } catch (Exception e) {
                log.warn("Spec vector batch skipped SKU {}: {}", skuId, e.getMessage());
                fail++;
            }
        }
        log.info("Spec vector batch complete: {} ok, {} failed", ok, fail);
    }

    @Override
    public List<EmbeddingSearchResult> findSimilarByVector(Long skuId, int limit) {
        int safeLimit = Math.min(Math.max(limit, 1), 20);
        String sql = """
                SELECT sku_id, 1 - (embedding <=> (SELECT embedding FROM product_embedding WHERE sku_id = ?)) AS similarity
                FROM product_embedding
                WHERE sku_id <> ?
                  AND embedding IS NOT NULL
                ORDER BY embedding <=> (SELECT embedding FROM product_embedding WHERE sku_id = ?)
                LIMIT ?
                """;
        return jdbcTemplate.query(sql,
                (rs, rowNum) -> new EmbeddingSearchResult(rs.getLong("sku_id"), rs.getDouble("similarity")),
                skuId, skuId, skuId, safeLimit);
    }

    private SkuEmbeddingSourceDto fetchSkuSource(Long skuId) {
        String sql = """
                SELECT s.id, s.name, c.name as category_name, b.name as brand_name,
                       s.description, s.specs::text
                FROM sku s
                LEFT JOIN category c ON c.id = s.category_id
                LEFT JOIN brand b ON b.id = s.brand_id
                WHERE s.id = ?
                """;
        return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> new SkuEmbeddingSourceDto(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getString("category_name"),
                rs.getString("brand_name"),
                rs.getString("description"),
                rs.getString("specs")
        ), skuId);
    }

    private long fetchPrice(Long skuId) {
        Long price = jdbcTemplate.queryForObject("SELECT price FROM sku WHERE id = ?", Long.class, skuId);
        return price == null ? 0L : price;
    }
}
