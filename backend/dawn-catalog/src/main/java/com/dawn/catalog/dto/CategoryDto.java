package com.dawn.catalog.dto;

import java.util.List;

public record CategoryDto(
        Long id,
        String name,
        String slug,
        Long parentId,
        List<CategoryDto> children) {
}
