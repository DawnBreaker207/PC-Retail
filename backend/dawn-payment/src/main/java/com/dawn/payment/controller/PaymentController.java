package com.dawn.payment.controller;

import com.dawn.common.exception.wrapper.ConflictException;
import com.dawn.common.exception.wrapper.InvalidRequestException;
import com.dawn.common.message.Message;
import com.dawn.common.security.AuthorizationConstants;
import com.dawn.payment.controller.request.CreatePaymentRequest;
import com.dawn.payment.dto.CreatePaymentResult;
import com.dawn.payment.dto.PaymentDto;
import com.dawn.payment.entity.enums.PaymentMethod;
import com.dawn.payment.infra.gateway.PaymentGatewayRegistry;
import com.dawn.payment.infra.vnpay.VnpayGatewayAdapter;
import com.dawn.payment.port.in.CreatePaymentUseCase;
import com.dawn.payment.port.in.QueryPaymentStatusUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final CreatePaymentUseCase createPaymentUseCase;
    private final QueryPaymentStatusUseCase queryPaymentStatusUseCase;
    private final PaymentGatewayRegistry gatewayRegistry;
    private final VnpayGatewayAdapter vnpayGateway;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @PostMapping("/create")
    @PreAuthorize(AuthorizationConstants.CUSTOMER_ONLY)
    public CreatePaymentResult create(
            @RequestHeader(value = "X-Idempotency-Key", required = false) String idempotencyKey,
            @RequestBody CreatePaymentRequest req,
            @AuthenticationPrincipal Long userId) {
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            String cacheKey = "payment:idempotency:" + idempotencyKey.trim();
            String cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                try {
                    return objectMapper.readValue(cached, CreatePaymentResult.class);
                } catch (Exception e) {
                    log.warn("Failed to deserialize cached payment idempotency result: {}", e.getMessage());
                }
            }

            Boolean locked = redisTemplate.opsForValue().setIfAbsent(cacheKey + ":lock", "1", Duration.ofSeconds(30));
            if (Boolean.FALSE.equals(locked)) {
                throw new ConflictException("Giao dịch thanh toán đang được xử lý, vui lòng không gửi lại.");
            }

            try {
                CreatePaymentResult result = createPaymentUseCase.create(req.orderId(), req.method(), userId);
                try {
                    redisTemplate.opsForValue().set(cacheKey, objectMapper.writeValueAsString(result), Duration.ofHours(24));
                } catch (Exception e) {
                    log.warn("Failed to cache payment idempotency result: {}", e.getMessage());
                }
                return result;
            } finally {
                redisTemplate.delete(cacheKey + ":lock");
            }
        }

        return createPaymentUseCase.create(req.orderId(), req.method(), userId);
    }

    @GetMapping("/callback/{provider}")
    public PaymentDto handleCallback(
            @PathVariable String provider,
            @RequestParam Map<String, String> params) {
        return queryPaymentStatusUseCase.handleCallback(provider, params);
    }

    @PostMapping("/webhook/{provider}")
    public PaymentDto handleWebhook(
            @PathVariable String provider,
            @RequestBody Map<String, String> payload) {
        return queryPaymentStatusUseCase.handleWebhook(provider, payload);
    }

    @GetMapping("/re-check/{provider}/{id}")
    @PreAuthorize(AuthorizationConstants.CUSTOMER_ONLY)
    public PaymentDto reCheck(
            @PathVariable String provider,
            @PathVariable Long id) {
        return queryPaymentStatusUseCase.queryAndSync(id);
    }

    // Mock gateway: dựng kịch bản cho demo/black-box (SUCCESS/FAILED/TIMEOUT/PENDING)
    @PostMapping("/mock/{id}/scenario")
    @PreAuthorize(AuthorizationConstants.ADMIN_ONLY)
    public Map<String, String> programMockScenario(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        com.dawn.payment.infra.mock.MockGatewayAdapter mock =
                (com.dawn.payment.infra.mock.MockGatewayAdapter) gatewayRegistry.get(
                        com.dawn.payment.entity.enums.PaymentMethod.MOCK);
        mock.programScenario(id, body.get("scenario"));
        return Map.of("paymentId", String.valueOf(id), "scenario", mock.scenarioOf(id));
    }
}
