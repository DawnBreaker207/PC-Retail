package com.dawn.payment.infra.gateway;

import com.dawn.common.message.Message;
import com.dawn.payment.dto.CreatePaymentResult;
import com.dawn.payment.dto.PaymentDto;
import com.dawn.payment.entity.Payment;
import com.dawn.payment.entity.enums.PaymentMethod;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class CashGateway implements PaymentGateway {
    @Override
    public PaymentMethod supportedMethod() {
        return PaymentMethod.CASH;
    }

    @Override
    public CreatePaymentResult createPayment(Payment payment) {
        return new CreatePaymentResult(payment.getId(), null);
    }

    @Override
    public PaymentDto processCallback(Long paymentId, Map<String, String> params) {
        throw new UnsupportedOperationException(Message.Payment.CALLBACK_NOT_SUPPORTED);
    }

    @Override
    public PaymentDto processWebhook(Long paymentId, Map<String, String> params) {
        throw new UnsupportedOperationException(Message.Payment.WEBHOOK_NOT_SUPPORTED);
    }

    @Override
    public boolean verifyCallback(Map<String, String> params) {
        return true;
    }

    @Override
    public PaymentDto queryStatus(Long paymentId, String transactionRef) {
        throw new UnsupportedOperationException(Message.Payment.CASH_NO_EXTERNAL_QUERY);
    }
}
