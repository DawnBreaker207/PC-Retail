package com.dawn.ai.controller;

import com.dawn.ai.service.FraudScorerService;
import com.dawn.common.response.ResponseObject;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequiredArgsConstructor
public class FraudController {

    private final FraudScorerService fraudScorerService;

    @PostMapping("/ai/fraud/score")
    public ResponseObject<FraudScorerService.FraudResult> score(
            @RequestParam Long userId,
            @RequestParam(required = false) String shippingAddress,
            @RequestParam BigDecimal totalAmount,
            @RequestParam(defaultValue = "1") int itemCount) {
        return ResponseObject.success(
                fraudScorerService.score(userId, shippingAddress, totalAmount, itemCount));
    }
}
