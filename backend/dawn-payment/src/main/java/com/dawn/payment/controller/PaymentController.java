package com.dawn.payment.controller;

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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final CreatePaymentUseCase createPaymentUseCase;
    private final QueryPaymentStatusUseCase queryPaymentStatusUseCase;
    private final PaymentGatewayRegistry gatewayRegistry;
    private final VnpayGatewayAdapter vnpayGateway;

    @PostMapping("/create")
    @PreAuthorize(AuthorizationConstants.CUSTOMER_ONLY)
    public CreatePaymentResult create(
            @RequestBody CreatePaymentRequest req,
            @AuthenticationPrincipal Long userId) {
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
}
