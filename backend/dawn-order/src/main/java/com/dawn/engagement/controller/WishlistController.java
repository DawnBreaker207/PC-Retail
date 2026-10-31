package com.dawn.engagement.controller;

import com.dawn.common.security.AuthorizationConstants;
import com.dawn.common.utils.SecurityUtils;
import com.dawn.engagement.entity.WishlistItem;
import com.dawn.engagement.service.WishlistCommandService;
import com.dawn.engagement.service.WishlistQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/wishlist")
@RequiredArgsConstructor
@PreAuthorize(AuthorizationConstants.CUSTOMER_ONLY)
public class WishlistController {

    private final WishlistQueryService wishlistQueryService;
    private final WishlistCommandService wishlistCommandService;

    @GetMapping
    public List<WishlistItem> list() {
        return wishlistQueryService.listByUser(SecurityUtils.getCurrentUserId());
    }

    @PostMapping
    public WishlistItem add(@RequestBody java.util.Map<String, Object> body) {
        Long skuId = Long.valueOf(body.get("skuId").toString());
        return wishlistCommandService.addItem(SecurityUtils.getCurrentUserId(), skuId);
    }

    @DeleteMapping("/{skuId}")
    public void remove(@PathVariable Long skuId) {
        wishlistCommandService.removeItem(SecurityUtils.getCurrentUserId(), skuId);
    }
}
