package com.dawn.order.controller;

import com.dawn.common.security.AuthorizationConstants;
import com.dawn.common.utils.SecurityUtils;
import com.dawn.order.controller.request.ReviewReturnRequest;
import com.dawn.order.controller.request.SubmitReturnRequest;
import com.dawn.order.dto.ReturnRequestDto;
import com.dawn.order.service.ReturnRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ReturnController {

    private final ReturnRequestService returnRequestService;

    // ── Customer endpoints ─────────────────────────────────────────────────

    @PostMapping("/orders/sub-orders/{subOrderId}/return")
    @PreAuthorize(AuthorizationConstants.CUSTOMER_ONLY)
    public ReturnRequestDto submit(@PathVariable Long subOrderId,
                                   @Valid @RequestBody SubmitReturnRequest req) {
        Long userId = SecurityUtils.getCurrentUserId();
        return returnRequestService.submit(subOrderId, userId, req.reason(), req.images());
    }

    @PostMapping("/orders/returns/{returnId}/dispute")
    @PreAuthorize(AuthorizationConstants.CUSTOMER_ONLY)
    public ReturnRequestDto dispute(@PathVariable Long returnId) {
        Long userId = SecurityUtils.getCurrentUserId();
        return returnRequestService.dispute(returnId, userId);
    }

    @GetMapping("/orders/returns/{returnId}")
    @PreAuthorize(AuthorizationConstants.CUSTOMER_ONLY)
    public ReturnRequestDto getById(@PathVariable Long returnId) {
        return returnRequestService.getById(returnId);
    }

    // ── Staff endpoints ────────────────────────────────────────────────────

    @PostMapping("/staff/returns/{returnId}/review")
    @PreAuthorize(AuthorizationConstants.STAFF_OR_ADMIN)
    public ReturnRequestDto review(@PathVariable Long returnId,
                                   @Valid @RequestBody ReviewReturnRequest req) {
        Long staffId = SecurityUtils.getCurrentUserId();
        return returnRequestService.review(returnId, staffId, req.approve(), req.note());
    }

    @PostMapping("/staff/returns/{returnId}/resolve-dispute")
    @PreAuthorize(AuthorizationConstants.STAFF_OR_ADMIN)
    public ReturnRequestDto resolveDispute(@PathVariable Long returnId,
                                           @Valid @RequestBody ReviewReturnRequest req) {
        Long staffId = SecurityUtils.getCurrentUserId();
        return returnRequestService.resolveDispute(returnId, staffId, req.approve(), req.note());
    }
}
