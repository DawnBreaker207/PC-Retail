package com.dawn.cart.controller;

import com.dawn.cart.controller.request.CartItemRequest;
import com.dawn.cart.dto.CartItemDto;
import com.dawn.cart.dto.CartSummaryDto;
import com.dawn.cart.port.in.AddCartItemUseCase;
import com.dawn.cart.port.in.GetCartUseCase;
import com.dawn.cart.port.in.RemoveCartItemUseCase;
import com.dawn.cart.port.in.UpdateCartItemUseCase;
import com.dawn.cart.service.CartCommandService;
import com.dawn.cart.service.CartQueryService;
import com.dawn.common.response.ResponseObject;
import com.dawn.common.security.AuthorizationConstants;
import com.dawn.common.utils.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final GetCartUseCase getCartUseCase;
    private final AddCartItemUseCase addCartItemUseCase;
    private final UpdateCartItemUseCase updateCartItemUseCase;
    private final RemoveCartItemUseCase removeCartItemUseCase;
    private final CartCommandService cartCommandService;
    private final CartQueryService cartQueryService;

    @GetMapping
    @PreAuthorize(AuthorizationConstants.CUSTOMER_ONLY)
    public ResponseObject<CartSummaryDto> getCart() {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseObject.success(getCartUseCase.getCart(userId));
    }

    @GetMapping("/count")
    @PreAuthorize(AuthorizationConstants.CUSTOMER_ONLY)
    public ResponseObject<Integer> getCount() {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseObject.success(getCartUseCase.getItemCount(userId));
    }

    @PostMapping("/items")
    @PreAuthorize(AuthorizationConstants.CUSTOMER_ONLY)
    public ResponseObject<CartItemDto> addItem(@Valid @RequestBody CartItemRequest req) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseObject.created(addCartItemUseCase.addItem(userId, req.skuId(), req.quantity()));
    }

    @PutMapping("/items")
    @PreAuthorize(AuthorizationConstants.CUSTOMER_ONLY)
    public ResponseObject<CartItemDto> updateItem(@Valid @RequestBody CartItemRequest req) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseObject.success(updateCartItemUseCase.updateItem(userId, req.skuId(), req.quantity()));
    }

    @DeleteMapping("/items/{skuId}")
    @PreAuthorize(AuthorizationConstants.CUSTOMER_ONLY)
    public ResponseObject<Void> removeItem(
            @PathVariable Long skuId) {
        Long userId = SecurityUtils.getCurrentUserId();
        removeCartItemUseCase.removeItem(userId, skuId);
        return ResponseObject.deleted();
    }

    // Guest cart: no JWT required, keyed by guest_token

    @GetMapping("/guest")
    public ResponseObject<CartSummaryDto> getGuestCart(@RequestParam String token) {
        return ResponseObject.success(cartQueryService.getGuestCart(token));
    }

    @PostMapping("/guest/items")
    public ResponseObject<CartItemDto> addGuestItem(
            @RequestParam String token, @Valid @RequestBody CartItemRequest req) {
        return ResponseObject.created(cartCommandService.addGuestItem(token, req.skuId(), req.quantity()));
    }

    @DeleteMapping("/guest/items/{skuId}")
    public ResponseObject<Void> removeGuestItem(
            @RequestParam String token, @PathVariable Long skuId) {
        cartCommandService.removeGuestItem(token, skuId);
        return ResponseObject.deleted();
    }

    @DeleteMapping("/guest")
    public ResponseObject<Void> clearGuestCart(@RequestParam String token) {
        cartCommandService.clearGuestCart(token);
        return ResponseObject.deleted();
    }

    @PostMapping("/merge")
    @PreAuthorize(AuthorizationConstants.CUSTOMER_ONLY)
    public ResponseObject<Void> mergeGuestCart(@RequestParam String token) {
        cartCommandService.mergeGuestCart(token, SecurityUtils.getCurrentUserId());
        return ResponseObject.success(null);
    }
}
