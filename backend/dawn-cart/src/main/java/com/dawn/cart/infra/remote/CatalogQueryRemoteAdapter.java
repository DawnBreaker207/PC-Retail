package com.dawn.cart.infra.remote;

import com.dawn.catalog.dto.CatalogQueryPort;
import com.dawn.catalog.dto.SkuPriceInfo;
import com.dawn.common.web.InternalRestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@ConditionalOnProperty(name = "app.services.mode", havingValue = "distributed")
public class CatalogQueryRemoteAdapter implements CatalogQueryPort {

    private final InternalRestClient client;
    private final String baseUrl;

    public CatalogQueryRemoteAdapter(InternalRestClient client,
                                     @Value("${app.services.catalog-url}") String baseUrl) {
        this.client = client;
        this.baseUrl = baseUrl;
    }

    @Override
    public Map<Long, SkuPriceInfo> getSkuDetailsMap(List<Long> skuIds) {
        return client.exchange(baseUrl + "/api/v1/internal/catalog/details", HttpMethod.POST, skuIds,
                new ParameterizedTypeReference<>() {
                });
    }
}
