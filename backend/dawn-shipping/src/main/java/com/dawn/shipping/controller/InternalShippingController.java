package com.dawn.shipping.controller;

import com.dawn.shipping.dto.ShippingFeeRequest;
import com.dawn.shipping.dto.ShippingFeeResponse;
import com.dawn.shipping.dto.ShippingOrderDto;
import com.dawn.shipping.service.ShippingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/shipping")
@RequiredArgsConstructor
public class InternalShippingController {

    private final ShippingService shippingService;

    @PostMapping("/fee")
    public ShippingFeeResponse calculateFee(@RequestBody ShippingFeeRequest request) {
        return shippingService.calculateFee(request);
    }

    @GetMapping("/order/{orderId}")
    public ShippingOrderDto getByOrder(@PathVariable Long orderId) {
        return shippingService.getByOrderId(orderId);
    }
}
