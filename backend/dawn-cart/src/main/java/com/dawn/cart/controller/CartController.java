package com.dawn.cart.controller;

import com.dawn.cart.controller.request.CartItemRequest;
import com.dawn.cart.dto.CartItemDto;
import com.dawn.cart.dto.CartQueryPort;
import com.dawn.cart.dto.CartSummaryDto;
import com.dawn.cart.port.in.AddCartItemUseCase;
import com.dawn.cart.port.in.GetCartUseCase;
import com.dawn.cart.port.in.RemoveCartItemUseCase;
import com.dawn.cart.port.in.UpdateCartItemUseCase;
import com.dawn.cart.service.CartService;
import com.dawn.common.response.ResponseObject;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final GetCartUseCase getCartUseCase;
    private final AddCartItemUseCase addCartItemUseCase;
    private final UpdateCartItemUseCase updateCartItemUseCase;
    private final RemoveCartItemUseCase removeCartItemUseCase;

    @GetMapping
    public ResponseObject<CartSummaryDto> getCart(@RequestHeader("X-User-Id") Long userId) {
        return ResponseObject.success(getCartUseCase.getCart(userId));
    }

    @GetMapping("/count")
    public ResponseObject<Integer> getCount(@RequestHeader("X-User-Id") Long userId) {
        return ResponseObject.success(getCartUseCase.getItemCount(userId));
    }

    @PostMapping("/items")
    public ResponseObject<CartItemDto> addItem(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody CartItemRequest req) {
        return ResponseObject.created(addCartItemUseCase.addItem(userId, req.skuId(), req.quantity()));
    }

    @PutMapping("/items/{skuId}")
    public ResponseObject<CartItemDto> updateItem(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long skuId,
            @Valid @RequestBody CartItemRequest req) {
        return ResponseObject.success(updateCartItemUseCase.updateItem(userId, skuId, req.quantity()));
    }

    @DeleteMapping("/items/{skuId}")
    public ResponseObject<Void> removeItem(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long skuId) {
        removeCartItemUseCase.removeItem(userId, skuId);
        return ResponseObject.deleted();
    }
}
