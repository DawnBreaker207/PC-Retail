package com.dawn.payment.infra.remote;

import com.dawn.common.web.InternalRestClient;
import com.dawn.payment.port.out.OrderClientPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;

@Component
@ConditionalOnProperty(name = "app.services.mode", havingValue = "distributed")
public class OrderRemoteAdapter implements OrderClientPort {

    private final InternalRestClient client;
    private final String baseUrl;

    public OrderRemoteAdapter(InternalRestClient client,
                              @Value("${app.services.order-url}") String baseUrl) {
        this.client = client;
        this.baseUrl = baseUrl;
    }

    @Override
    public BigDecimal getOrderTotalAmount(Long orderId) {
        return client.exchange(baseUrl + "/api/v1/internal/orders/" + orderId + "/total",
                HttpMethod.GET, null, new ParameterizedTypeReference<>() {
                });
    }

    @Override
    public Instant getOrderReservedUntil(Long orderId) {
        return client.exchange(baseUrl + "/api/v1/internal/orders/" + orderId + "/reserved-until",
                HttpMethod.GET, null, new ParameterizedTypeReference<>() {
                });
    }

    @Override
    public void markOrderPaid(Long orderId) {
        client.exchange(baseUrl + "/api/v1/internal/orders/" + orderId + "/paid",
                HttpMethod.POST, null, new ParameterizedTypeReference<Void>() {
                });
    }

    @Override
    public void markOrderPaymentFailed(Long orderId) {
        client.exchange(baseUrl + "/api/v1/internal/orders/" + orderId + "/payment-failed",
                HttpMethod.POST, null, new ParameterizedTypeReference<Void>() {
                });
    }
}
