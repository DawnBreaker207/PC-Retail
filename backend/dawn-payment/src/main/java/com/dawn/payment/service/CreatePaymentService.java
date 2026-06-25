package com.dawn.payment.service;

import com.dawn.common.exception.wrapper.InvalidRequestException;
import com.dawn.common.message.Message;
import com.dawn.payment.dto.CreatePaymentResult;
import com.dawn.payment.entity.Payment;
import com.dawn.payment.entity.enums.PaymentMethod;
import com.dawn.payment.entity.enums.PaymentResultStatus;
import com.dawn.payment.port.in.CreatePaymentUseCase;
import com.dawn.payment.port.out.OrderClientPort;
import com.dawn.payment.port.out.PaymentRepositoryPort;
import com.dawn.payment.port.out.VnpayGatewayPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CreatePaymentService implements CreatePaymentUseCase {

    private final PaymentRepositoryPort paymentRepo;
    private final OrderClientPort orderClient;
    private final VnpayGatewayPort vnpayGateway;

    @Override
    @Transactional
    public CreatePaymentResult create(Long orderId, PaymentMethod method, Long userId) {
        List<Payment> existing = paymentRepo.findByOrderIdAndMethod(orderId, method);
        Payment pending = existing.stream()
                .filter(p -> p.getStatus() == PaymentResultStatus.PENDING)
                .findFirst()
                .orElse(null);

        if (pending != null) {
            String url = vnpayGateway.buildPaymentUrl(pending.getId(), pending.getAmount(), "Order #" + orderId);
            return new CreatePaymentResult(pending.getId(), url);
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

        String redirectUrl = vnpayGateway.buildPaymentUrl(payment.getId(), amount, "Order #" + orderId);
        return new CreatePaymentResult(payment.getId(), redirectUrl);
    }
}
