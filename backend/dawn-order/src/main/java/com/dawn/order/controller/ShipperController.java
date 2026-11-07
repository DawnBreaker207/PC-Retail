package com.dawn.order.controller;

import com.dawn.common.response.ResponsePage;
import com.dawn.common.security.AuthorizationConstants;
import com.dawn.common.utils.SecurityUtils;
import com.dawn.order.dto.SubOrderDto;
import com.dawn.order.entity.enums.SubOrderStatus;
import com.dawn.order.port.in.TransitionSubOrderUseCase;
import com.dawn.order.port.out.SubOrderRepository;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/shipper")
@RequiredArgsConstructor
public class ShipperController {

    private final TransitionSubOrderUseCase transitionUseCase;
    private final SubOrderRepository subOrderRepo;

    /** List sub-orders assigned to the calling shipper. */
    @GetMapping("/my-orders")
    @PreAuthorize(AuthorizationConstants.SHIPPER_ONLY)
    public ResponsePage<SubOrderDto> myOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) SubOrderStatus status) {
        Long shipperId = SecurityUtils.getCurrentUserId();
        var pageable = PageRequest.of(page, size);
        var result = status != null
                ? subOrderRepo.findByShipperIdAndStatus(shipperId, status, pageable)
                : subOrderRepo.findByShipperId(shipperId, pageable);
        return ResponsePage.of(result.map(this::toDto));
    }

    /** Shipper picks up the order — READY_TO_SHIP → OUT_FOR_DELIVERY. */
    @PatchMapping("/sub-orders/{id}/pickup")
    @PreAuthorize(AuthorizationConstants.SHIPPER_ONLY)
    public SubOrderDto pickup(@PathVariable Long id) {
        Long shipperId = SecurityUtils.getCurrentUserId();
        assertAssignedToMe(id, shipperId);
        return transitionUseCase.transition(id, SubOrderStatus.OUT_FOR_DELIVERY, shipperId);
    }

    /** Shipper successfully delivers — OUT_FOR_DELIVERY → DELIVERED. */
    @PatchMapping("/sub-orders/{id}/delivered")
    @PreAuthorize(AuthorizationConstants.SHIPPER_ONLY)
    public SubOrderDto delivered(@PathVariable Long id,
                                 @RequestParam(defaultValue = "false") boolean codCollected) {
        Long shipperId = SecurityUtils.getCurrentUserId();
        assertAssignedToMe(id, shipperId);
        return transitionUseCase.deliver(id, shipperId, codCollected);
    }

    /** Delivery attempt failed — OUT_FOR_DELIVERY → DELIVERY_FAILED. */
    @PatchMapping("/sub-orders/{id}/failed")
    @PreAuthorize(AuthorizationConstants.SHIPPER_ONLY)
    public SubOrderDto failed(@PathVariable Long id,
                              @RequestParam @Size(max = 500) String note) {
        Long shipperId = SecurityUtils.getCurrentUserId();
        assertAssignedToMe(id, shipperId);
        return transitionUseCase.deliveryFailed(id, shipperId, note);
    }

    private void assertAssignedToMe(Long subOrderId, Long shipperId) {
        subOrderRepo.findById(subOrderId).ifPresent(so -> {
            if (!shipperId.equals(so.getShipperId())) {
                throw new com.dawn.common.exception.wrapper.InvalidRequestException(
                        "Sub-order not assigned to you");
            }
        });
    }

    private SubOrderDto toDto(com.dawn.order.entity.SubOrder so) {
        return new SubOrderDto(so.getId(), so.getOrderId(), so.getFulfillmentType(),
                so.getSubtotal(), so.getShippingFee(), so.getDiscountAmount(), so.getFinalAmount(),
                so.getStatus(), so.getShippingMethod(), so.getShipperId(),
                so.getDeliveryAttemptedAt(), so.getCodCollectedAt(), null);
    }
}
