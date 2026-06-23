package com.dawn.order.controller.request;

import com.dawn.order.entity.enums.SubOrderStatus;
import jakarta.validation.constraints.NotNull;

public record TransitionRequest(
        @NotNull SubOrderStatus status) {
}
