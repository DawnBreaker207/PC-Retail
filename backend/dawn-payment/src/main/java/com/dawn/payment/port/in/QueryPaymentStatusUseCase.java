package com.dawn.payment.port.in;

import com.dawn.payment.dto.PaymentDto;

import java.util.Map;

public interface QueryPaymentStatusUseCase {
    PaymentDto queryAndSync(Long paymentId);

    PaymentDto handleWebhook(String provider, Map<String, String> payload);

    PaymentDto handleCallback(String provider, Map<String, String> params);
}
