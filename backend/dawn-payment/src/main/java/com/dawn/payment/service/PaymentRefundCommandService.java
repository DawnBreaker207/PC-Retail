package com.dawn.payment.service;

import com.dawn.common.event.PaymentRefundedEvent;
import com.dawn.common.outbox.OutboxPublisher;
import com.dawn.payment.entity.Payment;
import com.dawn.payment.entity.enums.PaymentResultStatus;
import com.dawn.payment.port.out.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Shared refund logic used by multiple SAGA compensation consumers. */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentRefundCommandService {

    private final PaymentRepository paymentRepository;
    private final OutboxPublisher outboxPublisher;

    @Transactional
    public void processRefund(Long orderId, String reason) {
        List<Payment> payments = paymentRepository.findByOrderId(orderId);
        Payment completed = payments.stream()
                .filter(p -> p.getStatus() == PaymentResultStatus.COMPLETED)
                .findFirst()
                .orElse(null);

        if (completed == null) {
            log.warn("No COMPLETED payment for orderId={}, cannot refund (reason={})", orderId, reason);
            return;
        }

        if (completed.getStatus() == PaymentResultStatus.REFUNDED) {
            log.info("Payment for orderId={} already REFUNDED (idempotent skip)", orderId);
            return;
        }

        completed.setStatus(PaymentResultStatus.REFUNDED);
        paymentRepository.save(completed);

        outboxPublisher.publish("payment-events", "PAYMENT", completed.getId(),
                new PaymentRefundedEvent(orderId, completed.getTransactionRef()));

        log.info("Payment refunded for orderId={} reason={}", orderId, reason);
    }
}
