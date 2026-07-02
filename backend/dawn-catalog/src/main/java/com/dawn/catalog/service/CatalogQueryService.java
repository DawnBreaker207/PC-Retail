package com.dawn.catalog.service;

import com.dawn.catalog.dto.CatalogQueryPort;
import com.dawn.catalog.dto.SkuPriceInfo;
import com.dawn.catalog.entity.Sku;
import com.dawn.catalog.repository.SkuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CatalogQueryService implements CatalogQueryPort {
    private final SkuRepository skuRepository;

    @Transactional(readOnly = true)
    @Override
    public Map<Long, SkuPriceInfo> getSkuDetailsMap(List<Long> skuIds) {
        if (skuIds == null || skuIds.isEmpty()) {
            return Map.of();
        }
        return skuRepository.findAllActiveByIds(skuIds).stream()
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
    }
}
