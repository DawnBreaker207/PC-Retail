package com.dawn.rma.controller;

import com.dawn.common.security.AuthorizationConstants;
import com.dawn.common.utils.SecurityUtils;
import com.dawn.rma.entity.ReturnRequest;
import com.dawn.rma.service.ReturnService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ReturnController {

    private final ReturnService returnService;

    @PostMapping("/orders/{orderId}/return-requests")
    @PreAuthorize(AuthorizationConstants.CUSTOMER_ONLY)
    public ReturnRequest request(
            @PathVariable Long orderId,
            @RequestParam(required = false) Long subOrderId,
            @RequestBody Map<String, Object> body) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (subOrderId == null && body.get("subOrderId") != null) {
            subOrderId = Long.valueOf(body.get("subOrderId").toString());
        }
        if (subOrderId == null) {
            throw new com.dawn.common.exception.wrapper.InvalidRequestException("subOrderId is required");
        }
        String reason = (String) body.getOrDefault("reason", "");
        @SuppressWarnings("unchecked")
        List<String> images = (List<String>) body.getOrDefault("images", List.of());
        return returnService.request(subOrderId, userId, reason, images);
    }

    @GetMapping("/return-requests/sub-order/{subOrderId}")
    @PreAuthorize(AuthorizationConstants.CUSTOMER_ONLY)
    public ReturnRequest getBySubOrder(@PathVariable Long subOrderId) {
        return returnService.getBySubOrder(subOrderId, SecurityUtils.getCurrentUserId());
    }

    @GetMapping("/return-requests/{requestId}")
    @PreAuthorize(AuthorizationConstants.CUSTOMER_ONLY)
    public ReturnRequest get(@PathVariable Long requestId) {
        return returnService.get(requestId);
    }

    @GetMapping("/staff/return-requests")
    @PreAuthorize(AuthorizationConstants.STAFF_OR_ADMIN)
    public List<ReturnRequest> staffList(@RequestParam(required = false) String status) {
        return returnService.staffList(status);
    }

    @PostMapping("/return-requests/{requestId}/dispute")
    @PreAuthorize(AuthorizationConstants.CUSTOMER_ONLY)
    public ReturnRequest dispute(@PathVariable Long requestId,
                                 @RequestBody Map<String, Object> body) {
        Object note = body.get("note");
        return returnService.dispute(requestId, SecurityUtils.getCurrentUserId(),
                note == null ? null : note.toString());
    }

    @PutMapping("/admin/return-requests/{requestId}/resolve")
    @PreAuthorize(AuthorizationConstants.ADMIN_ONLY)
    public ReturnRequest resolve(@PathVariable Long requestId, @RequestBody Map<String, Object> body) {
        boolean overturn = Boolean.parseBoolean(String.valueOf(body.getOrDefault("overturn", "false")));
        Object note = body.get("note");
        return returnService.resolveDispute(requestId, SecurityUtils.getCurrentUserId(), overturn,
                note == null ? null : note.toString());
    }

    @PutMapping("/admin/return-requests/{requestId}/approve")
    @PreAuthorize(AuthorizationConstants.ADMIN_ONLY)
    public ReturnRequest approve(@PathVariable Long requestId, @RequestBody Map<String, Object> body) {
        Long staffId = SecurityUtils.getCurrentUserId();
        BigDecimal amount = body.get("amount") == null ? BigDecimal.ZERO
                : new BigDecimal(body.get("amount").toString());
        String method = (String) body.getOrDefault("refundMethod", "BANK_TRANSFER_MANUAL");
        return returnService.approve(requestId, staffId, amount, method);
    }

    @PutMapping("/admin/return-requests/{requestId}/reject")
    @PreAuthorize(AuthorizationConstants.ADMIN_ONLY)
    public ReturnRequest reject(@PathVariable Long requestId) {
        return returnService.reject(requestId, SecurityUtils.getCurrentUserId());
    }
}
