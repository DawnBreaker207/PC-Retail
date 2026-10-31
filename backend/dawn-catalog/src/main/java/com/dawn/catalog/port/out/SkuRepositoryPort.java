package com.dawn.catalog.port.out;

import com.dawn.catalog.entity.Sku;

import java.util.List;
import java.util.Optional;

/**
 * Output port — persistence contract for the Sku aggregate.
 * The JPA repository adapter implements this interface.
 */
public interface SkuRepositoryPort {

    Optional<Sku> findActiveById(Long id);

    List<Sku> findAllActiveByIds(List<Long> ids);

    boolean existsBySkuCode(String skuCode);

    boolean existsBySlug(String slug);

    Sku save(Sku sku);

    void deleteById(Long id);
}
