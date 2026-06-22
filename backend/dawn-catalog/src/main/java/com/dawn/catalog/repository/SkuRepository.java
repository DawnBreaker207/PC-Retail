package com.dawn.catalog.repository;

import com.dawn.catalog.entity.Sku;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SkuRepository extends JpaRepository<Sku, Long>, JpaSpecificationExecutor<Sku> {
    Optional<Sku> findByIdAndDeletedAtIsNull(Long id);

    boolean existsBySkuCodeAndDeletedAtIsNull(String skuCode);

    boolean existsBySlugAndDeletedAtIsNull(String slug);

    boolean existsByCategoryIdAndDeletedAtIsNull(Long categoryId);

    boolean existsByBrandIdAndDeletedAtIsNull(Long brandId);
}
