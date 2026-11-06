package com.dawn.order.controller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public record SubmitWarrantyClaimRequest(
        @NotBlank String customerName,
        @NotBlank @Pattern(regexp = "^(0|\\+84)[0-9]{8,10}$") String customerPhone,
        @NotBlank String description,
        @Size(max = 5) List<String> images,
        Long branchId
) {}
