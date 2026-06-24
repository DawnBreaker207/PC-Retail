package com.dawn.catalog.repository;

import com.dawn.catalog.entity.Sku;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SkuRepository extends JpaRepository<Sku, Long>, JpaSpecificationExecutor<Sku> {
    @Query("SELECT s FROM Sku s WHERE s.id IN :skuIds AND s.deletedAt IS NULL")
    List<Sku> findAllActiveByIds(@Param("skuIds") List<Long> skuIds);

    Optional<Sku> findByIdAndDeletedAtIsNull(Long id);

    boolean existsBySkuCodeAndDeletedAtIsNull(String skuCode);

    boolean existsBySlugAndDeletedAtIsNull(String slug);

    boolean existsByCategoryIdAndDeletedAtIsNull(Long categoryId);

    boolean existsByBrandIdAndDeletedAtIsNull(Long brandId);
}
