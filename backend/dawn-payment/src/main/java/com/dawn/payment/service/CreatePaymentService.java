package com.dawn.payment.service;

import com.dawn.common.exception.wrapper.ConflictException;
import com.dawn.common.exception.wrapper.InvalidRequestException;
import com.dawn.common.message.Message;
import com.dawn.payment.dto.CreatePaymentResult;
import com.dawn.payment.entity.Payment;
import com.dawn.payment.entity.enums.PaymentMethod;
import com.dawn.payment.entity.enums.PaymentResultStatus;
import com.dawn.payment.infra.gateway.PaymentGatewayRegistry;
import com.dawn.payment.port.in.CreatePaymentUseCase;
import com.dawn.payment.port.out.OrderClientPort;
import com.dawn.payment.port.out.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CreatePaymentService implements CreatePaymentUseCase {

    private final PaymentRepository paymentRepo;
    private final OrderClientPort orderClient;
    private final PaymentGatewayRegistry gatewayRegistry;

    @Override
    @Transactional
    public CreatePaymentResult create(Long orderId, PaymentMethod method, Long userId) {
        Instant reservedUntil = orderClient.getOrderReservedUntil(orderId);
        if (reservedUntil != null && reservedUntil.isBefore(Instant.now())) {
            throw new ConflictException(Message.Order.EXPIRED);
        }

        List<Payment> existing = paymentRepo.findByOrderIdAndMethod(orderId, method);
        Payment pending = existing.stream()
                .filter(p -> p.getStatus() == PaymentResultStatus.PENDING)
                .findFirst()
                .orElse(null);

        if (pending != null) {
            return gatewayRegistry.get(method).createPayment(pending);
        }

        BigDecimal amount = orderClient.getOrderTotalAmount(orderId);
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidRequestException(Message.Payment.INVALID_ORDER_AMOUNT);
        }

        Payment payment = Payment.builder()
                .orderId(orderId)
                .method(method)
                .amount(amount)
                .status(PaymentResultStatus.PENDING)
                .build();
        paymentRepo.save(payment);

        return gatewayRegistry.get(method).createPayment(payment);
    }
}
