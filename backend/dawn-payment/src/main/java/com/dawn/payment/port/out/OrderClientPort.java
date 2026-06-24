package com.dawn.payment.port.out;

import java.math.BigDecimal;

public interface OrderClientPort {
    BigDecimal getOrderTotalAmount(Long orderId);

    void markOrderPaid(Long orderId);

    void markOrderPaymentFailed(Long orderId);
}
