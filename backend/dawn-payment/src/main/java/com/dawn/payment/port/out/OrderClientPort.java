package com.dawn.payment.port.out;

import java.math.BigDecimal;
import java.time.Instant;

public interface OrderClientPort {
    BigDecimal getOrderTotalAmount(Long orderId);

    Instant getOrderReservedUntil(Long orderId);

    void markOrderPaid(Long orderId);

    void markOrderPaymentFailed(Long orderId);
}
