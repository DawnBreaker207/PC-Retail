package com.dawn.cart.service;

import com.dawn.cart.dto.CartItemDto;
import com.dawn.cart.port.out.CartQueryPort;
import com.dawn.cart.dto.CartSummaryDto;
import com.dawn.cart.entity.CartItem;
import com.dawn.cart.port.in.AddCartItemUseCase;
import com.dawn.cart.port.in.GetCartUseCase;
import com.dawn.cart.port.in.RemoveCartItemUseCase;
import com.dawn.cart.port.in.UpdateCartItemUseCase;
import com.dawn.cart.port.out.CartCachePort;
import com.dawn.cart.port.out.CartItemRepository;
import com.dawn.cart.port.out.InventoryQueryPort;
import com.dawn.catalog.dto.CatalogQueryPort;
import com.dawn.catalog.dto.SkuPriceInfo;
import com.dawn.common.exception.wrapper.ResourceInsufficientException;
import com.dawn.common.exception.wrapper.ResourceNotFoundException;
import com.dawn.common.message.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartService implements GetCartUseCase, AddCartItemUseCase, UpdateCartItemUseCase, RemoveCartItemUseCase, CartQueryPort {

    private final CartItemRepository cartItemRepository;
    private final CartCachePort cartCachePort;
    private final CatalogQueryPort catalogQueryPort;
    private final InventoryQueryPort inventoryQueryPort;

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
                String imageUrl = (skuInfo != null) ? skuInfo.primaryImageUrl() : "";

                BigDecimal subTotal = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));

                return new CartItemDto(
                        item.getSkuId(),
                        productName,
                        imageUrl,
                        unitPrice,
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
        return cartItemRepository.findByUserId(userId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    // WRITE
    @Retryable(
            retryFor = OptimisticLockingFailureException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 50, multiplier = 2))
    @Transactional
    @Override
    public CartItemDto addItem(Long userId, Long skuId, int quantity) {
        Integer existingQuantity = inventoryQueryPort.getAvailableQuantity(skuId);
        log.info("Get existing quantity {}", existingQuantity);
        CartItem item = cartItemRepository.findByUserIdAndSkuId(userId, skuId).map(existing -> {
            int newTotal = existing.getQuantity() + quantity;
            log.info("Get existing quantity {} and total in cart {}", existingQuantity, newTotal);
            if (newTotal > existingQuantity) {
                throw new ResourceInsufficientException(Message.Cart.INSUFFICIENT_STOCK.formatted(skuId, existingQuantity, newTotal));
            }
            existing.setQuantity(newTotal);
            return existing;
        }).orElseGet(() -> {
                    if (quantity > existingQuantity) {
                        throw new ResourceInsufficientException(Message.Cart.INSUFFICIENT_STOCK.formatted(skuId, existingQuantity, quantity));
                    }
                    return CartItem.builder()
                            .userId(userId)
                            .skuId(skuId)
                            .quantity(quantity)
                            .build();
                }
        );

        cartItemRepository.save(item);
        evictAfterCommit(userId);

        return toDto(item);
    }

    @Retryable(
            retryFor = OptimisticLockingFailureException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 50, multiplier = 2))
    @Transactional
    public CartItemDto updateItem(Long userId, Long skuId, int quantity) {
        CartItem item = cartItemRepository.findByUserIdAndSkuId(userId, skuId)
                .orElseThrow(() -> new ResourceNotFoundException(Message.format(Message.Cart.ITEM_NOT_FOUND, skuId)));
        item.setQuantity(quantity);
        cartItemRepository.save(item);

        evictAfterCommit(userId);
        return toDto(item);
    }

    @Transactional
    public void removeItem(Long userId, Long skuId) {
        cartItemRepository.deleteByUserIdAndSkuId(userId, skuId);
        evictAfterCommit(userId);
    }

    // Call AFTER_COMMIT from CheckoutService
    @Transactional
    public void clearCart(Long userId) {
        cartItemRepository.deleteByUserId(userId);
        evictAfterCommit(userId);
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

    private void evictAfterCommit(Long userId) {
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        cartCachePort.evict(userId);
                    }
                }
        );
    }

}
