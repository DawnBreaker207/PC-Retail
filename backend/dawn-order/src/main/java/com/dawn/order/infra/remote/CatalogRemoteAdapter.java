package com.dawn.order.infra.remote;

import com.dawn.order.port.out.CatalogClientPort;
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
    public Map<Long, BigDecimal> getPrices(List<Long> skuIds) {
        return feignClient.prices(skuIds);
    }

    @Override
    public Map<Long, String> getNames(List<Long> skuIds) {
        return feignClient.names(skuIds);
    }
}
