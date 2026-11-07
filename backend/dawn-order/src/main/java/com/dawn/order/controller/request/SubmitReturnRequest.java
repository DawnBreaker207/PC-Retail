package com.dawn.order.controller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record SubmitReturnRequest(
        @NotBlank String reason,
        @Size(max = 5) List<String> images,
        @NotBlank String bankAccount,
        @NotBlank String bankName,
        @NotBlank String accountHolder
) {}
