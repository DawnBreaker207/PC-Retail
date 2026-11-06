package com.dawn.order.controller;

import com.dawn.common.security.AuthorizationConstants;
import com.dawn.order.controller.request.SubmitWarrantyClaimRequest;
import com.dawn.order.dto.WarrantyClaimDto;
import com.dawn.order.dto.WarrantyItemDto;
import com.dawn.order.entity.enums.WarrantyClaimStatus;
import com.dawn.order.service.WarrantyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class WarrantyController {

    private final WarrantyService warrantyService;

    @GetMapping("/orders/sub-orders/{subOrderId}/warranty")
    @PreAuthorize(AuthorizationConstants.CUSTOMER_ONLY)
    public List<WarrantyItemDto> getWarrantyBySubOrder(@PathVariable Long subOrderId) {
        return warrantyService.getBySubOrder(subOrderId);
    }

    @PostMapping("/warranty/{warrantyItemId}/claims")
    @PreAuthorize(AuthorizationConstants.CUSTOMER_ONLY)
    public WarrantyClaimDto submitClaim(@PathVariable Long warrantyItemId,
                                        @Valid @RequestBody SubmitWarrantyClaimRequest req) {
        return warrantyService.submitClaim(warrantyItemId, req);
    }

    @GetMapping("/warranty/{warrantyItemId}/claims")
    @PreAuthorize(AuthorizationConstants.CUSTOMER_ONLY)
    public List<WarrantyClaimDto> getClaims(@PathVariable Long warrantyItemId) {
        return warrantyService.getClaimsByItem(warrantyItemId);
    }

    // ── Staff endpoints ────────────────────────────────────────────────────

    @PatchMapping("/staff/warranty/claims/{claimId}/status")
    @PreAuthorize(AuthorizationConstants.STAFF_OR_ADMIN)
    public WarrantyClaimDto updateStatus(@PathVariable Long claimId,
                                         @RequestParam WarrantyClaimStatus status,
                                         @RequestParam(required = false) String diagnosisNote,
                                         @RequestParam(required = false) String resolution) {
        Long staffId = null; // staffId not used in this call, can be added later
        return warrantyService.updateClaimStatus(claimId, status, diagnosisNote, resolution, staffId);
    }
}
