package com.dawn.catalog.service;

import com.dawn.catalog.dto.CatalogQueryPort;
import com.dawn.catalog.dto.SkuPriceInfo;
import com.dawn.catalog.entity.Sku;
import com.dawn.catalog.infra.cache.CatalogSkuCache;
import com.dawn.catalog.repository.SkuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CatalogQueryService implements CatalogQueryPort {
    private final SkuRepository skuRepository;
    private final ObjectProvider<CatalogSkuCache> cacheProvider;

    @Transactional(readOnly = true)
    @Override
    public Map<Long, SkuPriceInfo> getSkuDetailsMap(List<Long> skuIds) {
        if (skuIds == null || skuIds.isEmpty()) {
            return Map.of();
        }
        CatalogSkuCache cache = cacheProvider.getIfAvailable();
        if (cache != null) {
            var cached = cache.get(skuIds);
            if (cached.isPresent()) {
                return cached.get();
            }
        }
        Map<Long, SkuPriceInfo> details = skuRepository.findAllActiveByIds(skuIds).stream()
                .collect(Collectors.toMap(
                        Sku::getId,
                        sku -> new SkuPriceInfo(
                                sku.getId(),
                                sku.getName(),
                                sku.getImageUrls() != null
                                        && !sku.getImageUrls().isEmpty()
                                        ? sku.getImageUrls().getFirst()
                                        : null,
                                sku.getPrice())));
        if (cache != null) {
            cache.put(skuIds, details);
        }
        return details;
    }
}
