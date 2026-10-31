package com.dawn.shipping.controller;

import com.dawn.shipping.dto.ShippingFeeRequest;
import com.dawn.shipping.dto.ShippingFeeResponse;
import com.dawn.shipping.dto.ShippingOrderDto;
import com.dawn.shipping.port.in.QueryShipmentUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/shipping")
@RequiredArgsConstructor
public class InternalShippingController {

    private final QueryShipmentUseCase shippingQueryService;

    @PostMapping("/fee")
    public ShippingFeeResponse calculateFee(@RequestBody ShippingFeeRequest request) {
        return shippingQueryService.calculateFee(request);
    }

    @GetMapping("/order/{orderId}")
    public ShippingOrderDto getByOrder(@PathVariable Long orderId) {
        return shippingQueryService.getByOrderId(orderId);
    }
}
