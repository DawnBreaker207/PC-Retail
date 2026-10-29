package com.dawn.common.event;

public record PaymentFailedEvent(Long paymentId, Long orderId, String reason) {
}
