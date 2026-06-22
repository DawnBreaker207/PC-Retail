package com.dawn.catalog.controller.request;

import jakarta.validation.constraints.NotBlank;

public record BrandRequest(
        @NotBlank String name,
        String logoUrl) {
}
