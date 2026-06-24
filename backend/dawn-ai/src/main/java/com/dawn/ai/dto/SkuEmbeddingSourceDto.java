package com.dawn.ai.dto;

public record SkuEmbeddingSourceDto(
        Long skuId,
        String name,
        String categoryName,
        String brandName,
        String description,
        String specs) {

    public String toEmbeddingText() {
        return String.format(
                "Tên sản phẩm: %s. Danh mục: %s. Thương hiệu: %s. Mô tả: %s. Thông số kỹ thuật: %s",
                name, categoryName, brandName,
                description != null ? description : "Không có mô tả",
                specs != null ? specs : "Không có thông số"
        );
    }
}
