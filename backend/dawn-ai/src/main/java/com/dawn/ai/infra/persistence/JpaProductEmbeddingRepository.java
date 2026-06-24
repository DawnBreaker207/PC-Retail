package com.dawn.ai.infra.persistence;

import com.dawn.ai.entity.ProductEmbedding;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JpaProductEmbeddingRepository extends JpaRepository<ProductEmbedding, Long> {
    Optional<ProductEmbedding> findBySkuId(Long skuId);

    boolean existsBySkuId(Long skuId);

    @Query(value = """
            SELECT sku_id, 1 - (embedding <=> CAST(:queryVector AS vector)) AS similarity
            FROM product_embedding
            ORDER BY embedding <=> CAST(:queryVector AS vector)
            LIMIT :limit
            """, nativeQuery = true)
    List<Object[]> findTopKBySimilarity(
            @Param("queryVector") String queryVector,  // format: "[0.1, 0.2, ...]"
            @Param("limit") int limit
    );

    @Query(value = """
            SELECT s.id FROM sku s
            WHERE s.deleted_at IS NULL
              AND NOT EXISTS (
                  SELECT 1 FROM product_embedding pe WHERE pe.sku_id = s.id
              )
            """, nativeQuery = true)
    List<Long> findSkuIdsWithoutEmbedding();
}
