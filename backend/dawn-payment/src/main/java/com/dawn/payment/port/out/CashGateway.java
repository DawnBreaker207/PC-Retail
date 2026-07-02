package com.dawn.payment.port.out;

import com.dawn.common.message.Message;
import com.dawn.payment.dto.CreatePaymentResult;
import com.dawn.payment.dto.PaymentDto;
import com.dawn.payment.entity.Payment;
import com.dawn.payment.entity.enums.PaymentMethod;
import com.dawn.payment.infra.gateway.PaymentGateway;
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
    public PaymentDto queryStatus(Long paymentId, String transactionRef) {
        return null;
    }
}
