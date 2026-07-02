package com.dawn.payment.infra.gateway;

import com.dawn.payment.dto.CreatePaymentResult;
import com.dawn.payment.dto.PaymentDto;
import com.dawn.payment.entity.Payment;
import com.dawn.payment.entity.enums.PaymentMethod;

import java.util.Map;

public interface PaymentGateway {
    PaymentMethod supportedMethod();

    CreatePaymentResult createPayment(Payment payment);

    PaymentDto processCallback(Long paymentId, Map<String, String> params);

    PaymentDto processWebhook(Long paymentId, Map<String, String> params);

    PaymentDto queryStatus(Long paymentId, String transactionRef);
}
