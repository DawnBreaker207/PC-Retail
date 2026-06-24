package com.dawn.payment.port.in;

import com.dawn.payment.dto.PaymentDto;

public interface QueryPaymentStatusUseCase {
    PaymentDto queryAndSync(Long paymentId);
}
