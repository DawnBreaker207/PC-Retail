package com.dawn.payment.controller.request;

import com.dawn.payment.entity.enums.PaymentMethod;

public record CreatePaymentRequest(
        Long orderId,
        PaymentMethod method) {
}
