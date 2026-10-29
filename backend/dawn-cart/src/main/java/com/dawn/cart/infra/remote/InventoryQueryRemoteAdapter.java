package com.dawn.cart.infra.remote;

import com.dawn.cart.port.out.InventoryQueryPort;
import com.dawn.common.web.InternalRestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.services.mode", havingValue = "distributed")
public class InventoryQueryRemoteAdapter implements InventoryQueryPort {

    private final InternalRestClient client;
    private final String baseUrl;

    public InventoryQueryRemoteAdapter(InternalRestClient client,
                                       @Value("${app.services.inventory-url}") String baseUrl) {
        this.client = client;
        this.baseUrl = baseUrl;
    }

    @Override
    public Integer getAvailableQuantity(Long skuId) {
        return client.exchange(baseUrl + "/api/v1/internal/inventory/" + skuId + "/available",
                HttpMethod.GET, null, new ParameterizedTypeReference<>() {
                });
    }
}
