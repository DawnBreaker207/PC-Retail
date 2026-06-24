package com.dawn.payment.port.in;

import com.dawn.payment.dto.CreatePaymentResult;
import com.dawn.payment.entity.enums.PaymentMethod;

public interface CreatePaymentUseCase {
    CreatePaymentResult create(Long orderId, PaymentMethod method, Long userId);
}
