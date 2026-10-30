package com.dawn.payment.infra.remote;

import com.dawn.common.contract.OrderClientPort;
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
    public BigDecimal getOrderTotalAmount(Long orderId) {
        return feignClient.getOrderTotalAmount(orderId);
    }

    @Override
    public Instant getOrderReservedUntil(Long orderId) {
        return feignClient.getOrderReservedUntil(orderId);
    }

    @Override
    public void markOrderPaid(Long orderId) {
        feignClient.markOrderPaid(orderId);
    }

    @Override
    public void markOrderPaymentFailed(Long orderId) {
        feignClient.markOrderPaymentFailed(orderId);
    }
}
