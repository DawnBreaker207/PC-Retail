package com.dawn.wishlist.dto;

import com.dawn.wishlist.entity.WishlistItem;

import java.time.Instant;

public record WishlistItemDto(Long id, Long skuId, Instant addedAt) {
    public static WishlistItemDto from(WishlistItem item) {
        return new WishlistItemDto(item.getId(), item.getSkuId(), item.getAddedAt());
    }
}
