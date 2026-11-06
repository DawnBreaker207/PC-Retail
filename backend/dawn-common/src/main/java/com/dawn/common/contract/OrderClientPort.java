package com.dawn.common.contract;

import java.math.BigDecimal;
import java.time.Instant;

public interface OrderClientPort {
    BigDecimal getOrderTotalAmount(Long orderId);

    Instant getOrderReservedUntil(Long orderId);

    /** Returns the userId who owns this order, or null for guest orders. */
    Long getOrderOwnerId(Long orderId);

    void markOrderPaid(Long orderId);

    void markOrderPaymentFailed(Long orderId);
}
