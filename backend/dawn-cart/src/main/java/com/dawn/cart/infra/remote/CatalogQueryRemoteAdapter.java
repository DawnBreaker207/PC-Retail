package com.dawn.cart.infra.remote;

import com.dawn.catalog.dto.CatalogQueryPort;
import com.dawn.catalog.dto.SkuPriceInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.services.mode", havingValue = "distributed")
public class CatalogQueryRemoteAdapter implements CatalogQueryPort {

    private final CartCatalogFeignClient feignClient;

    @Override
    public Map<Long, SkuPriceInfo> getSkuDetailsMap(List<Long> skuIds) {
        return feignClient.details(skuIds);
    }
}
