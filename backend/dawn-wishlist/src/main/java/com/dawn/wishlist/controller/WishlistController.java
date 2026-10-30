package com.dawn.wishlist.controller;

import com.dawn.common.response.ResponseObject;
import com.dawn.common.utils.SecurityUtils;
import com.dawn.wishlist.dto.WishlistItemDto;
import com.dawn.wishlist.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;

    @GetMapping
    public ResponseObject<List<WishlistItemDto>> getWishlist() {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseObject.success(wishlistService.getWishlist(userId));
    }

    @PostMapping("/{skuId}")
    public ResponseObject<WishlistItemDto> addItem(@PathVariable Long skuId) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseObject.created(wishlistService.addItem(userId, skuId));
    }

    @DeleteMapping("/{skuId}")
    public ResponseObject<Void> removeItem(@PathVariable Long skuId) {
        Long userId = SecurityUtils.getCurrentUserId();
        wishlistService.removeItem(userId, skuId);
        return ResponseObject.deleted();
    }

    @GetMapping("/{skuId}/check")
    public ResponseObject<Boolean> checkWishlisted(@PathVariable Long skuId) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseObject.success(wishlistService.isWishlisted(userId, skuId));
    }
}
