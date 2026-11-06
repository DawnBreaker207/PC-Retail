package com.dawn.order.controller.request;

import jakarta.validation.constraints.NotNull;

public record ReviewReturnRequest(
        @NotNull Boolean approve,
        String note
) {}
