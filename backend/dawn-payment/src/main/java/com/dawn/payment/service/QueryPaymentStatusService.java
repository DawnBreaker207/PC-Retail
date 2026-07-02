package com.dawn.payment.service;

import com.dawn.common.exception.wrapper.ResourceNotFoundException;
import com.dawn.common.message.Message;
import com.dawn.payment.dto.PaymentDto;
import com.dawn.payment.dto.VnpayQueryResult;
import com.dawn.payment.entity.Payment;
import com.dawn.payment.entity.enums.PaymentResultStatus;
import com.dawn.payment.infra.gateway.PaymentGatewayRegistry;
import com.dawn.payment.infra.vnpay.VnpayGatewayAdapter;
import com.dawn.payment.port.in.QueryPaymentStatusUseCase;
import com.dawn.payment.port.out.OrderClientPort;
import com.dawn.payment.port.out.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class QueryPaymentStatusService implements QueryPaymentStatusUseCase {

    private final PaymentRepository paymentRepo;
    private final PaymentGatewayRegistry gatewayRegistry;
    private final OrderClientPort orderClient;

    @Override
    @Transactional
    public PaymentDto queryAndSync(Long paymentId) {
        Payment payment = paymentRepo.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException(Message.Payment.NOT_FOUND));

        // Solved payment before
        if (payment.getStatus() != PaymentResultStatus.PENDING) {
            return toDto(payment);
        }

        // Always call VNPAY Query API to get real status - Not trusted query param on URL redirect
        VnpayQueryResult result = ((VnpayGatewayAdapter) gatewayRegistry.get(payment.getMethod()))
                .queryTransaction(payment.getTransactionRef(), payment.getId());

        if (result.success()) {
            payment.setStatus(PaymentResultStatus.COMPLETED);
            payment.setTransactionRef(result.transactionNo());
            paymentRepo.save(payment);
            orderClient.markOrderPaid(payment.getOrderId());
        } else if ("UNKNOWN".equals(result.responseCode())) {
            payment.setStatus(PaymentResultStatus.UNKNOWN);
            paymentRepo.save(payment);
            log.warn("Payment {} status UNKNOWN after VNPay query, need to solve manual", paymentId);
        } else {
            payment.setStatus(PaymentResultStatus.FAILED);
            paymentRepo.save(payment);
            orderClient.markOrderPaymentFailed(payment.getOrderId());
        }

        return toDto(payment);
    }

    private PaymentDto toDto(Payment p) {
        return new PaymentDto(
                p.getId(),
                p.getOrderId(),
                p.getMethod(),
                p.getAmount(),
                p.getStatus(),
                p.getTransactionRef(),
                p.getCreatedAt());
    }
}
