package com.dawn.payment.port.out;

import com.dawn.payment.dto.VnpayQueryResult;

import java.math.BigDecimal;

public interface VnpayGatewayPort {
    String buildPaymentUrl(Long paymentId, BigDecimal amount, String orderInfo);

    VnpayQueryResult queryTransaction(String transactionRef, Long paymentId);
}
