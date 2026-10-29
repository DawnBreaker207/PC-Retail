package com.dawn.payment.service;

import com.dawn.common.exception.wrapper.InvalidRequestException;
import com.dawn.common.exception.wrapper.ResourceNotFoundException;
import com.dawn.common.event.PaymentCompletedEvent;
import com.dawn.common.event.PaymentFailedEvent;
import com.dawn.common.message.Message;
import com.dawn.common.outbox.OutboxPublisher;
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
    private final OutboxPublisher outboxPublisher;

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
            publishCompleted(payment);
            orderClient.markOrderPaid(payment.getOrderId());
        } else if ("UNKNOWN".equals(result.responseCode())) {
            payment.setStatus(PaymentResultStatus.UNKNOWN);
            paymentRepo.save(payment);
            log.warn("Payment {} status UNKNOWN after VNPay query", paymentId);
        } else if ("PENDING".equals(result.responseCode())) {
            // Mock PENDING: giữ nguyên, chờ lần sync sau
            log.info("Payment {} still PENDING at gateway", paymentId);
        } else {
            payment.setStatus(PaymentResultStatus.FAILED);
            paymentRepo.save(payment);
            publishFailed(payment, result.responseCode());
            orderClient.markOrderPaymentFailed(payment.getOrderId());
        }

        return toDto(payment);
    }

    @Override
    @Transactional
    public PaymentDto handleWebhook(String provider, Map<String, String> payload) {
        if ("mock".equals(provider)) {
            // Mock: dựng kịch bản từ params rồi đi chung luồng sync (idempotent như vnpay)
            com.dawn.payment.infra.mock.MockGatewayAdapter mock =
                    (com.dawn.payment.infra.mock.MockGatewayAdapter) gatewayRegistry.get(PaymentMethod.MOCK);
            String rawId = payload.get("paymentId");
            if (rawId == null) throw new InvalidRequestException("Mock webhook requires paymentId");
            Long paymentId = Long.parseLong(rawId);
            if (payload.get("result") != null) mock.programScenario(paymentId, payload.get("result"));
            return queryAndSync(paymentId);
        }
        if (!"vnpay".equals(provider)) {
            throw new UnsupportedOperationException(Message.Payment.WEBHOOK_NOT_SUPPORTED);
        }
        VnpayGatewayAdapter vnpay = (VnpayGatewayAdapter) gatewayRegistry.get(PaymentMethod.VNPAY);
        if (!vnpay.verifyCallback(payload)) {
            throw new InvalidRequestException(Message.Payment.VNPAY_WEBHOOK_INVALID_SIGNATURE);
        }
        Long paymentId = Long.parseLong(payload.get("vnp_TxnRef"));
        String responseCode = payload.get("vnp_ResponseCode");
        String transactionNo = payload.get("vnp_TransactionNo");

        Payment payment = paymentRepo.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException(Message.format(Message.Payment.NOT_FOUND, paymentId)));

        // Webhook duplicate/delayed: đã xử lý rồi thì trả về, không gọi markOrder* lần 2
        if (payment.getStatus() == PaymentResultStatus.COMPLETED
                || payment.getStatus() == PaymentResultStatus.FAILED) {
            return toDto(payment);
        }

        if ("00".equals(responseCode)) {
            payment.setStatus(PaymentResultStatus.COMPLETED);
            payment.setTransactionRef(transactionNo);
            payment.setSettledAt(Instant.now());
            paymentRepo.save(payment);
            publishCompleted(payment);
            orderClient.markOrderPaid(payment.getOrderId());
        } else {
            payment.setStatus(PaymentResultStatus.FAILED);
            paymentRepo.save(payment);
            publishFailed(payment, responseCode);
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
                throw new InvalidRequestException(Message.Payment.VNPAY_CALLBACK_INVALID_SIGNATURE);
            }
            paymentId = Long.parseLong(params.get("vnp_TxnRef"));
        } else if ("mock".equals(provider)) {
            com.dawn.payment.infra.mock.MockGatewayAdapter mock =
                    (com.dawn.payment.infra.mock.MockGatewayAdapter) gatewayRegistry.get(PaymentMethod.MOCK);
            if (!mock.verifyCallback(params)) {
                throw new InvalidRequestException("Mock callback requires paymentId");
            }
            paymentId = Long.parseLong(params.get("paymentId"));
            if (params.get("result") != null) mock.programScenario(paymentId, params.get("result"));
        } else {
            PaymentMethod method;
            try {
                method = PaymentMethod.valueOf(provider.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new InvalidRequestException(Message.format(Message.Payment.CALLBACK_PROVIDER_UNSUPPORTED, provider));
            }
            PaymentGateway gateway = gatewayRegistry.get(method);
            PaymentDto result = gateway.processCallback(null, params);
            if (result == null) throw new InvalidRequestException(Message.format(Message.Payment.CALLBACK_PROVIDER_UNSUPPORTED, provider));
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
        if (payment.getMethod() != PaymentMethod.VNPAY && payment.getMethod() != PaymentMethod.MOCK) {
            return null;
        }
        try {
            java.util.function.Supplier<VnpayQueryResult> query = () -> {
                if (payment.getMethod() == PaymentMethod.MOCK) {
                    com.dawn.payment.infra.mock.MockGatewayAdapter mock =
                            (com.dawn.payment.infra.mock.MockGatewayAdapter) gatewayRegistry.get(PaymentMethod.MOCK);
                    return mock.queryTransaction(payment.getTransactionRef(), payment.getId());
                }
                return ((VnpayGatewayAdapter) gatewayRegistry.get(payment.getMethod()))
                        .queryTransaction(payment.getTransactionRef(), payment.getId());
            };
            return CompletableFuture
                    .supplyAsync(query)
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

    private void publishCompleted(Payment payment) {
        outboxPublisher.publish("payment-events", "PAYMENT", payment.getId(),
                new PaymentCompletedEvent(
                        payment.getId(),
                        payment.getOrderId(),
                        payment.getAmount(),
                        payment.getTransactionRef(),
                        payment.getSettledAt()));
    }

    private void publishFailed(Payment payment, String reason) {
        outboxPublisher.publish("payment-events", "PAYMENT", payment.getId(),
                new PaymentFailedEvent(payment.getId(), payment.getOrderId(), reason));
    }

    private PaymentDto toDto(Payment p) {        return new PaymentDto(
                p.getId(),
                p.getOrderId(),
                p.getMethod(),
                p.getAmount(),
                p.getStatus(),
                p.getTransactionRef(),
                p.getCreatedAt());
    }
}
