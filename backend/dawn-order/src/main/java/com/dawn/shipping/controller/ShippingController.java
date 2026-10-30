package com.dawn.shipping.controller;

import com.dawn.common.response.ResponseObject;
import com.dawn.common.response.ResponsePage;
import com.dawn.common.utils.SecurityUtils;
import com.dawn.shipping.dto.CarrierWebhookRequest;
import com.dawn.shipping.dto.ShippingOrderDto;
import com.dawn.shipping.service.ShippingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/shipping")
@RequiredArgsConstructor
public class ShippingController {

    private final ShippingService shippingService;

    @GetMapping("/orders/{orderId}")
    public ResponseObject<ShippingOrderDto> getByOrder(@PathVariable Long orderId) {
        return ResponseObject.success(shippingService.getByOrderId(orderId));
    }

    @GetMapping("/my")
    public ResponseObject<ResponsePage<ShippingOrderDto>> myShipments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = SecurityUtils.getCurrentUserId();
        Pageable pageable = PageRequest.of(page, size);
        return ResponseObject.success(shippingService.listByUser(userId, pageable));
    }

    @PostMapping("/webhook/callback")
    public ResponseObject<Void> carrierCallback(@RequestBody CarrierWebhookRequest request) {
        shippingService.updateStatus(request.trackingCode(), request.status());
        return ResponseObject.success(null);
    }
}
