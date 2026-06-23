package com.dawn.cart.controller;

import com.dawn.cart.controller.request.CartItemRequest;
import com.dawn.cart.dto.CartItemDto;
import com.dawn.cart.dto.CartSummaryDto;
import com.dawn.cart.service.CartService;
import com.dawn.common.response.ResponseObject;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;


    @GetMapping
    public ResponseObject<CartSummaryDto> getCart(@RequestHeader("X-User-Id") Long userId) {
        return ResponseObject.success(cartService.getCart(userId));
    }

    @GetMapping("/count")
    public ResponseObject<Integer> getCount(@RequestHeader("X-User-Id") Long userId) {
        return ResponseObject.success(cartService.getItemCount(userId));
    }

    @PostMapping("/items")
    public ResponseObject<CartItemDto> addItem(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody CartItemRequest req) {
        return ResponseObject.created(cartService.addItem(userId, req.skuId(), req.quantity()));
    }

    @PutMapping("/items/{skuId}")
    public ResponseObject<CartItemDto> updateItem(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long skuId,
            @Valid @RequestBody CartItemRequest req) {
        return ResponseObject.success(cartService.updateItem(userId, skuId, req.quantity()));
    }

    @DeleteMapping("/items/{skuId}")
    public ResponseObject<Void> removeItem(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long skuId) {
        cartService.removeItem(userId, skuId);
        return ResponseObject.deleted();
    }
}
