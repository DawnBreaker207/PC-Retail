package com.dawn.ai.infra.persistence;

import com.dawn.ai.dto.EmbeddingSearchResult;
import com.dawn.ai.dto.SkuEmbeddingSourceDto;
import com.dawn.ai.entity.ProductEmbedding;
import com.dawn.ai.port.out.EmbeddingPort;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class PgvectorEmbeddingAdapter implements EmbeddingPort {

    private final JpaProductEmbeddingRepository repository;
    private final EmbeddingModel embeddingModel;
    private final JdbcTemplate jdbcTemplate;


    @Override
    public void generateAndSave(Long skuId) {
        SkuEmbeddingSourceDto source = fetchSkuSource(skuId);
        String text = source.toEmbeddingText();

        float[] vector = generateEmbedding(text);

        ProductEmbedding pe = repository.findBySkuId(skuId)
                .orElseGet(ProductEmbedding::new);
        pe.setSkuId(skuId);
        pe.setEmbedding(vector);
        pe.setEmbeddingText(text);
        pe.setModelVersion("text-embedding-ada-002");
        repository.save(pe);

        log.info("Saved embedding for SKU {}", skuId);
    }

    @Override
    public void generateBatch() {

    }

    @Override
    public List<EmbeddingSearchResult> searchSimilar(String query, int limit) {
        return List.of();
    }

    private float[] generateEmbedding(String text) {
        Embedding embedding = embeddingModel.embed(TextSegment.from(text)).content();
        return embedding.vectorAsList().stream()
                .map(Number::floatValue)
                .collect(Collectors.collectingAndThen(
                        Collectors.toList(),
                        list -> {
                            float[] arr = new float[list.size()];
                            for (int i = 0; i < list.size(); i++) arr[i] = list.get(i);
                            return arr;
                        }
                ));
    }

    private String toPostgresVectorString(float[] vector) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < vector.length; i++) {
            sb.append(vector[i]);
            if (i < vector.length - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
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
}
