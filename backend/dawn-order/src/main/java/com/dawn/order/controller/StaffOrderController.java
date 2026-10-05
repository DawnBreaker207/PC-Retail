package com.dawn.order.controller;

import com.dawn.common.security.AuthorizationConstants;
import com.dawn.common.utils.SecurityUtils;
import com.dawn.order.controller.request.TransitionRequest;
import com.dawn.order.dto.OrderDto;
import com.dawn.order.dto.SubOrderDto;
import com.dawn.order.port.in.QueryOrderUseCase;
import com.dawn.order.port.in.TransitionSubOrderUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/staff/orders")
@RequiredArgsConstructor
public class StaffOrderController {
    private final TransitionSubOrderUseCase transitionUseCase;
    private final QueryOrderUseCase queryOrderUseCase;

    @GetMapping
    @PreAuthorize(AuthorizationConstants.STAFF_OR_ADMIN)
    public com.dawn.common.response.ResponsePage<OrderDto> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return queryOrderUseCase.getAllForStaff(page, size);
    }

    @GetMapping("/{id}")
    @PreAuthorize(AuthorizationConstants.STAFF_OR_ADMIN)
    public OrderDto getById(@PathVariable Long id) {
        return queryOrderUseCase.getByIdForStaff(id);
    }

    @PostMapping("/sub-orders/{id}/transition")
    @PreAuthorize(AuthorizationConstants.STAFF_OR_ADMIN)
    public SubOrderDto transition(@PathVariable Long id, @Valid @RequestBody TransitionRequest req) {
        Long staffId = SecurityUtils.getCurrentUserId();
        return transitionUseCase.transition(id, req.status(), staffId);
    }
}
