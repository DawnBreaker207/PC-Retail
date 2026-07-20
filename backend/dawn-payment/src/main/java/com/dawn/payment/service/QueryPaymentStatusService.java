package com.dawn.payment.service;

import com.dawn.common.exception.wrapper.InvalidRequestException;
import com.dawn.common.exception.wrapper.ResourceNotFoundException;
import com.dawn.common.message.Message;
import com.dawn.payment.dto.PaymentDto;
import com.dawn.payment.dto.VnpayQueryResult;
import com.dawn.payment.entity.Payment;
import com.dawn.payment.entity.enums.PaymentMethod;
import com.dawn.payment.entity.enums.PaymentResultStatus;
import com.dawn.payment.infra.gateway.PaymentGateway;
import com.dawn.payment.infra.gateway.PaymentGatewayRegistry;
import com.dawn.payment.infra.vnpay.VnpayGatewayAdapter;
import com.dawn.payment.port.in.QueryPaymentStatusUseCase;
import com.dawn.payment.port.out.OrderClientPort;
import com.dawn.payment.port.out.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
@Slf4j
public class QueryPaymentStatusService implements QueryPaymentStatusUseCase {

    private static final Duration VNPAY_TIMEOUT = Duration.ofSeconds(5);

    private final PaymentRepository paymentRepo;
    private final PaymentGatewayRegistry gatewayRegistry;
    private final OrderClientPort orderClient;

    @Override
    @Transactional
    public PaymentDto queryAndSync(Long paymentId) {
        Payment payment = paymentRepo.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException(Message.format(Message.Payment.NOT_FOUND, paymentId)));

        if (payment.getStatus() != PaymentResultStatus.PENDING) {
            return toDto(payment);
        }

        VnpayQueryResult result = queryWithTimeout(payment);
        if (result == null) {
            payment.setStatus(PaymentResultStatus.UNKNOWN);
            paymentRepo.save(payment);
            log.warn("Payment {} VNPay query timed out, marked UNKNOWN for retry", paymentId);
            return toDto(payment);
        }

        if (result.success()) {
            payment.setStatus(PaymentResultStatus.COMPLETED);
            payment.setTransactionRef(result.transactionNo());
            payment.setSettledAt(Instant.now());
            paymentRepo.save(payment);
            orderClient.markOrderPaid(payment.getOrderId());
        } else if ("UNKNOWN".equals(result.responseCode())) {
            payment.setStatus(PaymentResultStatus.UNKNOWN);
            paymentRepo.save(payment);
            log.warn("Payment {} status UNKNOWN after VNPay query", paymentId);
        } else {
            payment.setStatus(PaymentResultStatus.FAILED);
            paymentRepo.save(payment);
            orderClient.markOrderPaymentFailed(payment.getOrderId());
        }

        return toDto(payment);
    }

    @Override
    @Transactional
    public PaymentDto handleWebhook(String provider, Map<String, String> payload) {
        if (!"vnpay".equals(provider)) {
            throw new UnsupportedOperationException(Message.Payment.WEBHOOK_NOT_SUPPORTED);
        }
        VnpayGatewayAdapter vnpay = (VnpayGatewayAdapter) gatewayRegistry.get(PaymentMethod.VNPAY);
        if (!vnpay.verifyCallback(payload)) {
            throw new InvalidRequestException("Invalid VNPay webhook signature");
        }
        Long paymentId = Long.parseLong(payload.get("vnp_TxnRef"));
        String responseCode = payload.get("vnp_ResponseCode");
        String transactionNo = payload.get("vnp_TransactionNo");

        Payment payment = paymentRepo.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException(Message.format(Message.Payment.NOT_FOUND, paymentId)));

        if ("00".equals(responseCode)) {
            payment.setStatus(PaymentResultStatus.COMPLETED);
            payment.setTransactionRef(transactionNo);
            payment.setSettledAt(Instant.now());
            paymentRepo.save(payment);
            orderClient.markOrderPaid(payment.getOrderId());
        } else {
            payment.setStatus(PaymentResultStatus.FAILED);
            paymentRepo.save(payment);
            orderClient.markOrderPaymentFailed(payment.getOrderId());
        }

        return toDto(payment);
    }

    @Override
    @Transactional
    public PaymentDto handleCallback(String provider, Map<String, String> params) {
        Long paymentId;
        if ("vnpay".equals(provider)) {
            VnpayGatewayAdapter vnpay = (VnpayGatewayAdapter) gatewayRegistry.get(PaymentMethod.VNPAY);
            if (!vnpay.verifyCallback(params)) {
                throw new InvalidRequestException("Invalid VNPay callback signature");
            }
            paymentId = Long.parseLong(params.get("vnp_TxnRef"));
        } else {
            PaymentMethod method = PaymentMethod.valueOf(provider.toUpperCase());
            PaymentGateway gateway = gatewayRegistry.get(method);
            PaymentDto result = gateway.processCallback(null, params);
            if (result == null) throw new InvalidRequestException("Unsupported callback provider: " + provider);
            return result;
        }

        return queryAndSync(paymentId);
    }

    @Scheduled(fixedDelay = 120_000)
    @Transactional
    public void resolveUnresolvedPayment() {
        List<Payment> unknown = paymentRepo.findByStatus(PaymentResultStatus.UNKNOWN);
        unknown.forEach(p -> queryAndSync(p.getId()));
    }

    private VnpayQueryResult queryWithTimeout(Payment payment) {
        if (payment.getMethod() != PaymentMethod.VNPAY) {
            return null;
        }
        try {
            return CompletableFuture
                    .supplyAsync(() -> ((VnpayGatewayAdapter) gatewayRegistry.get(payment.getMethod()))
                            .queryTransaction(payment.getTransactionRef(), payment.getId()))
                    .orTimeout(VNPAY_TIMEOUT.toMillis(), java.util.concurrent.TimeUnit.MILLISECONDS)
                    .join();
        } catch (CancellationException | CompletionException e) {
            if (e.getCause() instanceof java.util.concurrent.TimeoutException
                    || e.getMessage() != null && e.getMessage().contains("timed out")) {
                log.warn("VNPay query timed out after {}ms for payment {}", VNPAY_TIMEOUT.toMillis(), payment.getId());
            } else {
                log.error("VNPay query failed for payment {}", payment.getId(), e);
            }
            return null;
        }
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
