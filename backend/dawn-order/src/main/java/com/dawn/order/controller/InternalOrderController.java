package com.dawn.order.controller;

import com.dawn.common.contract.OrderQueryPort;
import com.dawn.common.contract.OrderClientPort;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import java.math.BigDecimal;
import java.time.Instant;

/** API nội bộ cho payment-service/catalog-service; bảo vệ bằng X-Internal-Token. */
@RestController
@RequestMapping("/internal/orders")
@RequiredArgsConstructor
public class InternalOrderController {

    private final OrderClientPort orderClientPort;
    private final OrderQueryPort orderQueryPort;

    @GetMapping("/{orderId}/total")
    public BigDecimal total(@PathVariable Long orderId) {
        return orderClientPort.getOrderTotalAmount(orderId);
    }

    @GetMapping("/{orderId}/reserved-until")
    public Instant reservedUntil(@PathVariable Long orderId) {
        return orderClientPort.getOrderReservedUntil(orderId);
    }

    @GetMapping("/{orderId}/owner")
    public Long owner(@PathVariable Long orderId) {
        return orderClientPort.getOrderOwnerId(orderId);
    }

    @PostMapping("/{orderId}/paid")
    public void paid(@PathVariable Long orderId) {
        orderClientPort.markOrderPaid(orderId);
    }

    @PostMapping("/{orderId}/payment-failed")
    public void paymentFailed(@PathVariable Long orderId) {
        orderClientPort.markOrderPaymentFailed(orderId);
    }

    @GetMapping("/items/exists")
    public boolean itemExists(@RequestParam Long skuId) {
        return orderQueryPort.existsOrderItemBySkuId(skuId);
    }

    @GetMapping("/sub-orders/{subOrderId}/delivered")
    public boolean delivered(@PathVariable Long subOrderId) {
        return orderQueryPort.isSubOrderDelivered(subOrderId);
    }

    @GetMapping("/verify-purchase")
    public Map<String, Boolean> verifyPurchase(@RequestParam Long userId, @RequestParam Long skuId) {
        boolean purchased = orderQueryPort.hasUserPurchasedSku(userId, skuId);
        return Map.of("purchased", purchased);
    }
}
