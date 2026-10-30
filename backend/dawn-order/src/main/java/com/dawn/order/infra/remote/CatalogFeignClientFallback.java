package com.dawn.order.infra.remote;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class CatalogFeignClientFallback implements CatalogFeignClient {

    @Override
    public Map<Long, BigDecimal> prices(List<Long> skuIds) {
        log.warn("Catalog service unavailable — returning empty prices for {} skus", skuIds.size());
        return Map.of();
    }

    @Override
    public Map<Long, String> names(List<Long> skuIds) {
        log.warn("Catalog service unavailable — returning empty names for {} skus", skuIds.size());
        return Map.of();
    }
}
