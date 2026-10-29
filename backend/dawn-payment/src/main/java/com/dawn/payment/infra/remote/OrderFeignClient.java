package com.dawn.payment.infra.remote;

import com.dawn.common.feign.FeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.math.BigDecimal;
import java.time.Instant;

@FeignClient(
        name = "order-service",
        contextId = "paymentOrderFeignClient",
        url = "${app.services.order-url:http://order-service:8081}",
        path = "/api/v1/internal/orders",
        configuration = FeignClientConfig.class
)
public interface OrderFeignClient {

    @GetMapping("/{orderId}/total")
    BigDecimal getOrderTotalAmount(@PathVariable("orderId") Long orderId);

    @GetMapping("/{orderId}/reserved-until")
    Instant getOrderReservedUntil(@PathVariable("orderId") Long orderId);

    @PostMapping("/{orderId}/paid")
    void markOrderPaid(@PathVariable("orderId") Long orderId);

    @PostMapping("/{orderId}/payment-failed")
    void markOrderPaymentFailed(@PathVariable("orderId") Long orderId);
}
