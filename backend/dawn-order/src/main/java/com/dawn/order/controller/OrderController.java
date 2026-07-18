package com.dawn.order.controller;

import com.dawn.common.response.ResponsePage;
import com.dawn.common.security.AuthorizationConstants;
import com.dawn.common.utils.SecurityUtils;
import com.dawn.order.controller.request.CheckoutRequest;
import com.dawn.order.dto.OrderDto;
import com.dawn.order.dto.SubOrderDto;
import com.dawn.order.port.in.CheckoutUseCase;
import com.dawn.order.port.in.QueryOrderUseCase;
import com.dawn.order.port.in.TransitionSubOrderUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
    private final CheckoutUseCase checkoutUseCase;
    private final QueryOrderUseCase queryOrderUseCase;
    private final TransitionSubOrderUseCase transitionUseCase;

    @PostMapping("/checkout")
    @PreAuthorize(AuthorizationConstants.CUSTOMER_ONLY)
    public OrderDto checkout(@Valid @RequestBody CheckoutRequest req) {
        Long userId = SecurityUtils.getCurrentUserId();
        return checkoutUseCase.checkout(req, userId);
    }


    @GetMapping
    @PreAuthorize(AuthorizationConstants.CUSTOMER_ONLY)
    public ResponsePage<OrderDto> getMyOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = SecurityUtils.getCurrentUserId();
        return queryOrderUseCase.getMyOrders(userId, page, size);
    }

    @GetMapping("/{id}")
    @PreAuthorize(AuthorizationConstants.CUSTOMER_ONLY)
    public OrderDto getById(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        return queryOrderUseCase.getById(id, userId);
    }


    @PostMapping("/sub-orders/{id}/cancel")
    @PreAuthorize(AuthorizationConstants.CUSTOMER_ONLY)
    public SubOrderDto cancel(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        return transitionUseCase.cancel(id, userId);
    }
}

