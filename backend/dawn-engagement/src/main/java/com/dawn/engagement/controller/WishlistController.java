package com.dawn.engagement.controller;

import com.dawn.common.security.AuthorizationConstants;
import com.dawn.common.utils.SecurityUtils;
import com.dawn.engagement.entity.WishlistItem;
import com.dawn.engagement.infra.persistence.JpaWishlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/wishlist")
@RequiredArgsConstructor
@PreAuthorize(AuthorizationConstants.CUSTOMER_ONLY)
public class WishlistController {

    private final JpaWishlistRepository wishlistRepo;

    @GetMapping
    public List<WishlistItem> list() {
        return wishlistRepo.findByUserId(SecurityUtils.getCurrentUserId());
    }

    @PostMapping
    public WishlistItem add(@RequestBody java.util.Map<String, Object> body) {
        Long skuId = Long.valueOf(body.get("skuId").toString());
        Long userId = SecurityUtils.getCurrentUserId();
        return wishlistRepo.findByUserIdAndSkuId(userId, skuId)
                .orElseGet(() -> wishlistRepo.save(WishlistItem.builder().userId(userId).skuId(skuId).build()));
    }

    @DeleteMapping("/{skuId}")
    public void remove(@PathVariable Long skuId) {
        wishlistRepo.findByUserIdAndSkuId(SecurityUtils.getCurrentUserId(), skuId)
                .ifPresent(wishlistRepo::delete);
    }
}
