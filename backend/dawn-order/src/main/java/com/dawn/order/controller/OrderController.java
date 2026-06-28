package com.dawn.order.controller;

import com.dawn.common.response.ResponsePage;
import com.dawn.common.utils.SecurityUtils;
import com.dawn.order.controller.request.CheckoutRequest;
import com.dawn.order.dto.OrderDto;
import com.dawn.order.dto.SubOrderDto;
import com.dawn.order.port.in.CheckoutUseCase;
import com.dawn.order.port.in.QueryOrderUseCase;
import com.dawn.order.port.in.TransitionSubOrderUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
    private final CheckoutUseCase checkoutUseCase;
    private final QueryOrderUseCase queryOrderUseCase;
    private final TransitionSubOrderUseCase transitionUseCase;

    @PostMapping("/checkout")
    public OrderDto checkout(@RequestBody CheckoutRequest req) {
        Long userId = SecurityUtils.getCurrentUserId();
        return checkoutUseCase.checkout(req, userId);
    }


    @GetMapping
    public ResponsePage<OrderDto> getMyOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = SecurityUtils.getCurrentUserId();
        return queryOrderUseCase.getMyOrders(userId, page, size);
    }

    @GetMapping("/{id}")
    public OrderDto getById(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        return queryOrderUseCase.getById(id, userId);
    }


    @PostMapping("/sub-orders/{id}/cancel")
    public SubOrderDto cancel(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        return transitionUseCase.cancel(id, userId);
    }
}

