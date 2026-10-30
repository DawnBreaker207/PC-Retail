package com.dawn.order.infra.remote;

import com.dawn.common.contract.CatalogClientPort;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
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
    @Bulkhead(name = "catalog")
    public Map<Long, BigDecimal> getPrices(List<Long> skuIds) {
        return feignClient.prices(skuIds);
    }

    @Override
    @Bulkhead(name = "catalog")
    public Map<Long, String> getNames(List<Long> skuIds) {
        return feignClient.names(skuIds);
    }
}
