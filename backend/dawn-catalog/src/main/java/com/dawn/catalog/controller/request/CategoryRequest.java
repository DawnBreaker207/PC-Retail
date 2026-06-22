package com.dawn.catalog.controller.request;

import jakarta.validation.constraints.NotBlank;

public record CategoryRequest(
        @NotBlank String name,
        @NotBlank String slug,
        Long parentId) {
}
