package com.dawn.payment.infra.remote;

import com.dawn.common.contract.OrderClientPort;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.services.mode", havingValue = "distributed")
public class OrderRemoteAdapter implements OrderClientPort {

    private final OrderFeignClient feignClient;

    @Override
    @Bulkhead(name = "order")
    public BigDecimal getOrderTotalAmount(Long orderId) {
        return feignClient.getOrderTotalAmount(orderId);
    }

    @Override
    @Bulkhead(name = "order")
    public Instant getOrderReservedUntil(Long orderId) {
        return feignClient.getOrderReservedUntil(orderId);
    }

    @Override
    @Bulkhead(name = "order")
    public Long getOrderOwnerId(Long orderId) {
        return feignClient.getOrderOwnerId(orderId);
    }

    @Override
    @Bulkhead(name = "order")
    public void markOrderPaid(Long orderId) {
        feignClient.markOrderPaid(orderId);
    }

    @Override
    @Bulkhead(name = "order")
    public void markOrderPaymentFailed(Long orderId) {
        feignClient.markOrderPaymentFailed(orderId);
    }
}
