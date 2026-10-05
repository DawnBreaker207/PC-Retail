package com.dawn.order.controller;

import com.dawn.order.controller.request.GuestCheckoutRequest;
import com.dawn.order.dto.OrderDto;
import com.dawn.order.service.GuestCheckoutService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class GuestOrderController {

    private final GuestCheckoutService guestCheckoutService;

    // Khách vãng lai: không cần JWT
    @PostMapping("/guest-checkout")
    public OrderDto guestCheckout(@Valid @RequestBody GuestCheckoutRequest req) {
        return guestCheckoutService.checkout(req);
    }
}
