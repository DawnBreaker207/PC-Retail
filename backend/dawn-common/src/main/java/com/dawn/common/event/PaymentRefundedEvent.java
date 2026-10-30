package com.dawn.common.event;

public record PaymentRefundedEvent(Long orderId, String transactionRef) {}
