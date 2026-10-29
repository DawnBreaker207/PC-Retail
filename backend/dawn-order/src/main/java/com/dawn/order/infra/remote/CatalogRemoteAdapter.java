package com.dawn.order.infra.remote;

import com.dawn.common.web.InternalRestClient;
import com.dawn.order.port.out.CatalogClientPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Component
@ConditionalOnProperty(name = "app.services.mode", havingValue = "distributed")
public class CatalogRemoteAdapter implements CatalogClientPort {

    private final InternalRestClient client;
    private final String baseUrl;

    public CatalogRemoteAdapter(InternalRestClient client,
                                @Value("${app.services.catalog-url}") String baseUrl) {
        this.client = client;
        this.baseUrl = baseUrl;
    }

    @Override
    public Map<Long, BigDecimal> getPrices(List<Long> skuIds) {
        return client.exchange(baseUrl + "/api/v1/internal/catalog/prices", HttpMethod.POST, skuIds,
                new ParameterizedTypeReference<>() {
                });
    }

    @Override
    public Map<Long, String> getNames(List<Long> skuIds) {
        return client.exchange(baseUrl + "/api/v1/internal/catalog/names", HttpMethod.POST, skuIds,
                new ParameterizedTypeReference<>() {
                });
    }
}
