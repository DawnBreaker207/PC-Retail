package com.dawn.payment.infra.mock;

import com.dawn.payment.dto.CreatePaymentResult;
import com.dawn.payment.dto.PaymentDto;
import com.dawn.payment.dto.VnpayQueryResult;
import com.dawn.payment.entity.Payment;
import com.dawn.payment.entity.enums.PaymentMethod;
import com.dawn.payment.entity.enums.PaymentResultStatus;
import com.dawn.payment.infra.gateway.PaymentGateway;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Mock gateway cho dev/demo/black-box (thay MoMo/VNPay thật).
 * Kịch bản lập trình được cho từng payment: SUCCESS (mặc định), FAILED,
 * TIMEOUT (→ UNKNOWN + cron retry), PENDING (giữ nguyên).
 */
@Component
public class MockGatewayAdapter implements PaymentGateway {

    public static final String SCENARIO_SUCCESS = "SUCCESS";
    public static final String SCENARIO_FAILED = "FAILED";
    public static final String SCENARIO_TIMEOUT = "TIMEOUT";
    public static final String SCENARIO_PENDING = "PENDING";

    private final ConcurrentHashMap<Long, String> scenarios = new ConcurrentHashMap<>();

    @Override
    public PaymentMethod supportedMethod() {
        return PaymentMethod.MOCK;
    }

    public void programScenario(Long paymentId, String scenario) {
        String normalized = scenario == null ? SCENARIO_SUCCESS : scenario.toUpperCase();
        if (!SCENARIO_SUCCESS.equals(normalized) && !SCENARIO_FAILED.equals(normalized)
                && !SCENARIO_TIMEOUT.equals(normalized) && !SCENARIO_PENDING.equals(normalized)) {
            throw new IllegalArgumentException("Unknown mock scenario: " + scenario);
        }
        scenarios.put(paymentId, normalized);
    }

    public String scenarioOf(Long paymentId) {
        if (paymentId == null) return SCENARIO_SUCCESS;
        return scenarios.getOrDefault(paymentId, SCENARIO_SUCCESS);
    }

    @Override
    public CreatePaymentResult createPayment(Payment payment) {
        return new CreatePaymentResult(payment.getId(), "mock://pay/" + payment.getId());
    }

    @Override
    public PaymentDto processCallback(Long paymentId, Map<String, String> params) {
        Long id = paymentId != null ? paymentId : parseId(params.get("paymentId"));
        String result = params == null ? null : params.get("result");
        if (result != null) {
            programScenario(id, result);
        }
        return preview(id);
    }

    @Override
    public PaymentDto processWebhook(Long paymentId, Map<String, String> params) {
        // supports webhook unlike VNPay, for idempotency demo
        return processCallback(paymentId, params);
    }

    @Override
    public boolean verifyCallback(Map<String, String> params) {
        return params != null && params.get("paymentId") != null;
    }

    @Override
    public PaymentDto queryStatus(Long paymentId, String transactionRef) {
        VnpayQueryResult result = queryTransaction(String.valueOf(transactionRef), paymentId);
        PaymentResultStatus status = result.success()
                ? PaymentResultStatus.COMPLETED
                : ("UNKNOWN".equals(result.responseCode()) || "NO_RESPONSE".equals(result.responseCode())
                        ? PaymentResultStatus.UNKNOWN
                        : "PENDING".equals(result.responseCode())
                                ? PaymentResultStatus.PENDING
                                : PaymentResultStatus.FAILED);
        return new PaymentDto(paymentId, null, PaymentMethod.MOCK, null, status,
                result.transactionNo(), null);
    }

    public VnpayQueryResult queryTransaction(String transactionRef, Long paymentId) {
        String scenario = scenarioOf(paymentId);
        return switch (scenario) {
            case SCENARIO_FAILED -> new VnpayQueryResult(false, "MOCK-" + paymentId, "01");
            case SCENARIO_TIMEOUT -> throw new RuntimeException("mock gateway timeout");
            case SCENARIO_PENDING -> new VnpayQueryResult(false, null, "PENDING");
            default -> new VnpayQueryResult(true, "MOCK-" + paymentId, "00");
        };
    }

    private PaymentDto preview(Long id) {
        return new PaymentDto(id, null, PaymentMethod.MOCK, null,
                PaymentResultStatus.PENDING, null, null);
    }

    private Long parseId(String raw) {
        if (raw == null) throw new IllegalArgumentException("Mock callback requires paymentId");
        return Long.parseLong(raw);
    }
}
