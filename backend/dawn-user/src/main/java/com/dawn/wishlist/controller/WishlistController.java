package com.dawn.wishlist.controller;

import com.dawn.common.response.ResponseObject;
import com.dawn.common.utils.SecurityUtils;
import com.dawn.wishlist.dto.WishlistItemDto;
import com.dawn.wishlist.service.WishlistCommandService;
import com.dawn.wishlist.service.WishlistQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistCommandService wishlistCommandService;
    private final WishlistQueryService wishlistQueryService;

    @GetMapping
    public ResponseObject<List<WishlistItemDto>> getWishlist() {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseObject.success(wishlistQueryService.listByUser(userId));
    }

    @PostMapping("/{skuId}")
    public ResponseObject<WishlistItemDto> addItem(@PathVariable Long skuId) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseObject.created(wishlistCommandService.addItem(userId, skuId));
    }

    @DeleteMapping("/{skuId}")
    public ResponseObject<Void> removeItem(@PathVariable Long skuId) {
        Long userId = SecurityUtils.getCurrentUserId();
        wishlistCommandService.removeItem(userId, skuId);
        return ResponseObject.deleted();
    }

    @GetMapping("/{skuId}/check")
    public ResponseObject<Boolean> checkWishlisted(@PathVariable Long skuId) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseObject.success(wishlistQueryService.existsInWishlist(userId, skuId));
    }
}
