package com.dawn.order.infra.remote;

import com.dawn.common.contract.CatalogClientPort;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.services.mode", havingValue = "distributed")
public class CatalogRemoteAdapter implements CatalogClientPort {

    private final CatalogFeignClient feignClient;

    @Override
    @CircuitBreaker(name = "catalog", fallbackMethod = "getPricesFallback")
    @Bulkhead(name = "catalog")
    public Map<Long, BigDecimal> getPrices(List<Long> skuIds) {
        return feignClient.prices(skuIds);
    }

    @Override
    @CircuitBreaker(name = "catalog", fallbackMethod = "getNamesFallback")
    @Bulkhead(name = "catalog")
    public Map<Long, String> getNames(List<Long> skuIds) {
        return feignClient.names(skuIds);
    }

    // getPrices is critical for checkout — throw to let caller fail fast
    Map<Long, BigDecimal> getPricesFallback(List<Long> skuIds, Throwable t) {
        log.error("Catalog circuit OPEN — getPrices failed: {}", t.getMessage());
        throw new RuntimeException("Catalog service unavailable: " + t.getMessage(), t);
    }

    // getNames is display-only — return empty map as graceful degradation
    Map<Long, String> getNamesFallback(List<Long> skuIds, Throwable t) {
        log.warn("Catalog circuit OPEN — getNames degraded to empty: {}", t.getMessage());
        return Map.of();
    }
}
