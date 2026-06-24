package com.dawn.order.controller;

import com.dawn.common.response.ResponsePage;
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
    public OrderDto checkout(@RequestBody CheckoutRequest req, @AuthenticationPrincipal Long userId) {
        return checkoutUseCase.checkout(req, userId);
    }


    @GetMapping
    public ResponsePage<OrderDto> getMyOrders(
            @AuthenticationPrincipal Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return queryOrderUseCase.getMyOrders(userId, page, size);
    }

    @GetMapping("/{id}")
    public OrderDto getById(@PathVariable Long id, @AuthenticationPrincipal Long userId) {
        return queryOrderUseCase.getById(id, userId);
    }


    @PostMapping("/sub-orders/{id}/cancel")
    public SubOrderDto cancel(@PathVariable Long id, @AuthenticationPrincipal Long userId) {
        return transitionUseCase.cancel(id, userId);
    }
}

