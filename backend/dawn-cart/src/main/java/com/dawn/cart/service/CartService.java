package com.dawn.cart.service;

import com.dawn.cart.dto.CartItemDto;
import com.dawn.cart.dto.CartSummaryDto;
import com.dawn.cart.entity.CartItem;
import com.dawn.cart.repository.CartItemRepository;
import com.dawn.common.exception.wrapper.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {
    private static final String CART_KEY = "cart:count";
    private static final long CART_TTL_HOURS = 24;

    private final CartItemRepository cartItemRepository;
    private final StringRedisTemplate redisTemplate;

    // READ
    public CartSummaryDto getCart(Long userId) {
        List<CartItem> items = cartItemRepository.findByUserId(userId);

        List<CartItemDto> dtos = items.stream()
                .map(item -> new CartItemDto(
                        item.getSkuId(),
                        null,
                        null,
                        BigDecimal.ZERO,
                        item.getQuantity(),
                        BigDecimal.ZERO
                ))
                .toList();

        BigDecimal total = dtos.stream()
                .map(CartItemDto::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartSummaryDto(dtos, dtos.size(), total);
    }

    public int getItemCount(Long userId) {
        String cached = redisTemplate.opsForValue().get(CART_KEY + userId);
        if (cached != null) return Integer.parseInt(cached);

        int count = cartItemRepository.countByUserId(userId);
        redisTemplate.opsForValue().set(CART_KEY + userId, String.valueOf(count), CART_TTL_HOURS, TimeUnit.HOURS);
        return count;
    }


    // WRITE
    @Retryable(retryFor = OptimisticLockingFailureException.class, maxAttempts = 3)
    @Transactional
    public CartItemDto addItem(Long userId, Long skuId, int quantity) {
        CartItem item = cartItemRepository.findByUserIdAndSkuId(userId, skuId).map(existing -> {
            existing.setQuantity(existing.getQuantity() + quantity);
            return existing;
        }).orElseGet(() -> CartItem.builder()
                .userId(userId)
                .skuId(skuId)
                .quantity(quantity)
                .build());

        cartItemRepository.save(item);
        invalidateCountCache(userId);

        return toDto(item);
    }
    @Retryable(retryFor = OptimisticLockingFailureException.class, maxAttempts = 3)
    @Transactional
    public CartItemDto updateItem(Long userId, Long skuId, int quantity) {
        CartItem item = cartItemRepository.findByUserIdAndSkuId(userId, skuId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));
        item.setQuantity(quantity);
        cartItemRepository.save(item);
        invalidateCountCache(userId);

        return toDto(item);
    }

    @Transactional
    public void removeItem(Long userId, Long skuId) {
        cartItemRepository.deleteByUserIdAndSkuId(userId, skuId);
        invalidateCountCache(userId);
    }

    // Call AFTER_COMMIT from CheckoutService
    @Transactional
    public void clearCart(Long userId) {
        cartItemRepository.deleteByUserId(userId);
        redisTemplate.delete(CART_KEY + userId);
    }

    // Take raw cart items for CheckoutService
    @Transactional(readOnly = true)
    public List<CartItem> getRawItems(Long userId) {
        return cartItemRepository.findByUserId(userId);
    }

    // Private Helpers
    private void invalidateCountCache(Long userId) {
        redisTemplate.delete(CART_KEY + userId);
    }

    private CartItemDto toDto(CartItem item) {
        return new CartItemDto(
                item.getSkuId(),
                null,
                null,
                BigDecimal.ZERO,
                item.getQuantity(),
                BigDecimal.ZERO);
    }
}
