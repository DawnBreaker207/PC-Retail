package com.dawn.catalog.bridge;

import com.dawn.catalog.entity.Sku;
import com.dawn.catalog.repository.SkuRepository;
import com.dawn.common.entity.BaseEntity;
import com.dawn.order.port.out.CatalogClientPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CatalogClientAdapter implements CatalogClientPort {

    private final SkuRepository skuRepository;

    @Override
    public Map<Long, BigDecimal> getPrices(List<Long> skuIds) {
        return skuRepository.findAllActiveByIds(skuIds)
                .stream()
                .collect(Collectors.toMap(BaseEntity::getId, Sku::getPrice));
    }

    @Override
    public Map<Long, String> getNames(List<Long> skuIds) {
        return skuRepository.findAllActiveByIds(skuIds)
                .stream()
                .collect(Collectors.toMap(BaseEntity::getId, Sku::getName));
    }
}
