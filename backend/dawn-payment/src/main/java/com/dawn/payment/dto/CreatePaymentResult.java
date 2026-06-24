package com.dawn.payment.dto;

public record CreatePaymentResult(
        Long paymentId,
        String redirectUrl) {
}
