package com.dawn.order.controller;

import com.dawn.common.security.AuthorizationConstants;
import com.dawn.order.port.in.MockCarrierUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/sub-orders")
@RequiredArgsConstructor
public class CarrierController {
    private final MockCarrierUseCase carrierService;

    @PostMapping("/{subOrderId}/assign-carrier")
    @PreAuthorize(AuthorizationConstants.STAFF_ONLY)
    public void assignCarrier(@PathVariable Long subOrderId, @RequestBody Map<String, String> body) {
        carrierService.assignCarrier(subOrderId, body.get("carrierName"));
    }

    @PostMapping("/{subOrderId}/deliver")
    @PreAuthorize(AuthorizationConstants.STAFF_ONLY)
    public void deliver(@PathVariable Long subOrderId) {
        carrierService.mockDeliveryWebhook(subOrderId);
    }
}
