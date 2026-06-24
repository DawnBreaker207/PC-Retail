package com.dawn.payment.controller;

import com.dawn.payment.controller.request.CreatePaymentRequest;
import com.dawn.payment.dto.CreatePaymentResult;
import com.dawn.payment.dto.PaymentDto;
import com.dawn.payment.port.in.CreatePaymentUseCase;
import com.dawn.payment.port.in.QueryPaymentStatusUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final CreatePaymentUseCase createPaymentUseCase;
    private final QueryPaymentStatusUseCase queryPaymentStatusUseCase;

    @PostMapping
    public CreatePaymentResult create(
            @RequestBody CreatePaymentRequest req,
            @AuthenticationPrincipal Long userId) {
        return createPaymentUseCase.create(req.orderId(), req.method(), userId);
    }


    @GetMapping("/{paymentId}/return")
    public PaymentDto handleReturn(@PathVariable Long paymentId) {
        return queryPaymentStatusUseCase.queryAndSync(paymentId);
    }
}
