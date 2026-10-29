package com.dawn.catalog.infra.remote;

import com.dawn.common.web.InternalRestClient;
import com.dawn.order.dto.OrderQueryPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.services.mode", havingValue = "distributed")
public class OrderQueryRemoteAdapter implements OrderQueryPort {

    private final InternalRestClient client;
    private final String baseUrl;

    public OrderQueryRemoteAdapter(InternalRestClient client,
                                   @Value("${app.services.order-url}") String baseUrl) {
        this.client = client;
        this.baseUrl = baseUrl;
    }

    @Override
    public boolean existsOrderItemBySkuId(Long skuId) {
        return Boolean.TRUE.equals(client.exchange(
                baseUrl + "/api/v1/internal/orders/items/exists?skuId=" + skuId,
                HttpMethod.GET, null, new ParameterizedTypeReference<>() {
                }));
    }

    @Override
    public boolean isSubOrderDelivered(Long subOrderId) {
        return Boolean.TRUE.equals(client.exchange(
                baseUrl + "/api/v1/internal/orders/sub-orders/" + subOrderId + "/delivered",
                HttpMethod.GET, null, new ParameterizedTypeReference<>() {
                }));
    }
}
