package com.dawn.cart.service;

import com.dawn.cart.dto.CartItemDto;
import com.dawn.cart.dto.CartQueryPort;
import com.dawn.cart.dto.CartSummaryDto;
import com.dawn.cart.entity.CartItem;
import com.dawn.cart.infra.persistence.JpaCartItemRepository;
import com.dawn.cart.port.in.AddCartItemUseCase;
import com.dawn.cart.port.in.GetCartUseCase;
import com.dawn.cart.port.in.RemoveCartItemUseCase;
import com.dawn.cart.port.in.UpdateCartItemUseCase;
import com.dawn.cart.port.out.CartCachePort;
import com.dawn.catalog.dto.CatalogQueryPort;
import com.dawn.catalog.dto.SkuPriceInfo;
import com.dawn.common.exception.wrapper.ResourceNotFoundException;
import com.dawn.common.message.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartService implements GetCartUseCase, AddCartItemUseCase, UpdateCartItemUseCase, RemoveCartItemUseCase, CartQueryPort {

    private final JpaCartItemRepository cartItemRepository;
    private final CartCachePort cartCachePort;
    private final CatalogQueryPort catalogQueryPort;

    // READ
    @Override
    public CartSummaryDto getCart(Long userId) {
        return cartCachePort.getCart(userId).orElseGet(() -> {
            List<CartItem> items = cartItemRepository.findByUserId(userId);
            if (items.isEmpty()) {
                return new CartSummaryDto(List.of(), 0, BigDecimal.ZERO);
            }
            List<Long> skuIds = items.stream().map(CartItem::getSkuId).toList();
            Map<Long, SkuPriceInfo> skuInfoMap = catalogQueryPort.getSkuDetailsMap(skuIds);

            List<CartItemDto> dtos = items.stream().map(item -> {
                SkuPriceInfo skuInfo = skuInfoMap.get(item.getSkuId());
                BigDecimal unitPrice = (skuInfo != null) ? skuInfo.unitPrice() : BigDecimal.ZERO;
                String productName = (skuInfo != null) ? skuInfo.productName() : "";
                String skuName = (skuInfo != null) ? skuInfo.skuName() : "";

                BigDecimal subTotal = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));

                return new CartItemDto(
                        item.getSkuId(),
                        productName,
                        skuName, unitPrice,
                        item.getQuantity(),
                        subTotal);
            }).toList();

            BigDecimal totalAmount = dtos.stream()
                    .map(CartItemDto::subtotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            CartSummaryDto summary = new CartSummaryDto(dtos, dtos.size(), totalAmount);
            cartCachePort.putCart(userId, summary);
            return summary;
        });
    }

    public int getItemCount(Long userId) {
        return cartCachePort.getItemCount(userId).orElseGet(() -> {
            int count = cartItemRepository.countByUserId(userId);
            cartCachePort.putItemCount(userId, count);
            return count;
        });
    }


    @Override
    public List<CartItemDto> getItems(Long userId) {
        return List.of();
    }

    // WRITE
    @Retryable(retryFor = OptimisticLockingFailureException.class, maxAttempts = 3)
    @Transactional
    @Override
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
        cartCachePort.evict(userId);

        return toDto(item);
    }

    @Retryable(retryFor = OptimisticLockingFailureException.class, maxAttempts = 3)
    @Transactional
    public CartItemDto updateItem(Long userId, Long skuId, int quantity) {
        CartItem item = cartItemRepository.findByUserIdAndSkuId(userId, skuId)
                .orElseThrow(() -> new ResourceNotFoundException(Message.Cart.ITEM_NOT_FOUND));
        item.setQuantity(quantity);
        cartItemRepository.save(item);

        cartCachePort.evict(userId);
        return toDto(item);
    }

    @Transactional
    public void removeItem(Long userId, Long skuId) {
        cartItemRepository.deleteByUserIdAndSkuId(userId, skuId);
        cartCachePort.evict(userId);
    }

    // Call AFTER_COMMIT from CheckoutService
    @Transactional
    public void clearCart(Long userId) {
        cartItemRepository.deleteByUserId(userId);
        cartCachePort.evict(userId);
    }

    // Take raw cart items for CheckoutService
    @Transactional(readOnly = true)
    public List<CartItem> getRawItems(Long userId) {
        return cartItemRepository.findByUserId(userId);
    }

    // Private Helpers
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
