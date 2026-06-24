package com.dawn.order.controller;

import com.dawn.order.controller.request.TransitionRequest;
import com.dawn.order.dto.SubOrderDto;
import com.dawn.order.port.in.TransitionSubOrderUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/staff/orders")
@RequiredArgsConstructor
public class StaffOrderController {
    private final TransitionSubOrderUseCase transitionUseCase;

    @PostMapping("/sub-orders/{id}/transition")
    public SubOrderDto transition(@PathVariable Long id, @RequestBody TransitionRequest req, @AuthenticationPrincipal Long staffId) {
        return transitionUseCase.transition(id, req.status(), staffId);
    }
}
