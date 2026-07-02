package com.dawn.payment.controller;

import com.dawn.common.message.Message;
import com.dawn.payment.controller.request.CreatePaymentRequest;
import com.dawn.payment.dto.CreatePaymentResult;
import com.dawn.payment.dto.PaymentDto;
import com.dawn.payment.port.in.CreatePaymentUseCase;
import com.dawn.payment.port.in.QueryPaymentStatusUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final CreatePaymentUseCase createPaymentUseCase;
    private final QueryPaymentStatusUseCase queryPaymentStatusUseCase;

    @PostMapping("/create")
    public CreatePaymentResult create(
            @RequestBody CreatePaymentRequest req,
            @AuthenticationPrincipal Long userId) {
        return createPaymentUseCase.create(req.orderId(), req.method(), userId);
    }

    @GetMapping("/callback/{provider}")
    public PaymentDto handleCallback(
            @PathVariable String provider,
            @RequestParam Map<String, String> params) {
        Long paymentId = Long.parseLong(params.get("vnp_TxnRef"));
        return queryPaymentStatusUseCase.queryAndSync(paymentId);
    }

    @PostMapping("/webhook/{provider}")
    public PaymentDto handleWebhook(
            @PathVariable String provider,
            @RequestBody Map<String, String> payload) {
        throw new UnsupportedOperationException(Message.Payment.WEBHOOK_NOT_SUPPORTED);
    }

    @GetMapping("/re-check/{provider}/{id}")
    public PaymentDto reCheck(
            @PathVariable String provider,
            @PathVariable Long id) {
        return queryPaymentStatusUseCase.queryAndSync(id);
    }
}
