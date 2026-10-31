package com.dawn.cart.service;

import com.dawn.cart.dto.CartItemDto;
import com.dawn.cart.entity.CartItem;
import com.dawn.cart.port.in.AddCartItemUseCase;
import com.dawn.cart.port.in.RemoveCartItemUseCase;
import com.dawn.cart.port.in.UpdateCartItemUseCase;
import com.dawn.cart.port.out.CartCachePort;
import com.dawn.cart.port.out.CartItemRepository;
import com.dawn.common.contract.InventoryQueryPort;
import com.dawn.common.exception.wrapper.InvalidRequestException;
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

@Service
@RequiredArgsConstructor
@Slf4j
public class CartCommandService implements AddCartItemUseCase, UpdateCartItemUseCase, RemoveCartItemUseCase {

    private final CartItemRepository cartItemRepository;
    private final CartCachePort cartCachePort;
    private final InventoryQueryPort inventoryQueryPort;

    @Retryable(
            retryFor = {
                    org.springframework.orm.ObjectOptimisticLockingFailureException.class,
                    org.springframework.dao.DataIntegrityViolationException.class
            },
            maxAttempts = 3,
            backoff = @Backoff(delay = 50, multiplier = 2))
    @Transactional
    @Override
    public CartItemDto addItem(Long userId, Long skuId, int quantity) {
        if (quantity <= 0) {
            throw new InvalidRequestException(Message.Cart.INVALID_QUANTITY);
        }
        Integer available = inventoryQueryPort.getAvailableQuantity(skuId);
        int newTotal = cartItemRepository.findByUserIdAndSkuId(userId, skuId)
                .map(CartItem::getQuantity).orElse(0) + quantity;
        if (available != null && newTotal > available) {
            throw new ResourceInsufficientException(Message.format(Message.Cart.INSUFFICIENT_STOCK, skuId, available, newTotal));
        }

        cartItemRepository.upsertMemberQuantity(userId, skuId, newTotal);
        evictAfterCommit(userId);

        return toDto(CartItem.builder().skuId(skuId).quantity(newTotal).build());
    }

    @Retryable(
            retryFor = OptimisticLockingFailureException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 50, multiplier = 2))
    @Transactional
    @Override
    public CartItemDto updateItem(Long userId, Long skuId, int quantity) {
        if (quantity <= 0) {
            throw new InvalidRequestException(Message.Cart.INVALID_QUANTITY);
        }
        CartItem item = cartItemRepository.findByUserIdAndSkuId(userId, skuId)
                .orElseThrow(() -> new ResourceNotFoundException(Message.format(Message.Cart.ITEM_NOT_FOUND, skuId)));
        Integer available = inventoryQueryPort.getAvailableQuantity(skuId);
        if (available != null && quantity > available) {
            throw new ResourceInsufficientException(Message.format(Message.Cart.INSUFFICIENT_STOCK, skuId, available, quantity));
        }
        item.setQuantity(quantity);
        cartItemRepository.save(item);

        evictAfterCommit(userId);
        return toDto(item);
    }

    @Transactional
    @Override
    public void removeItem(Long userId, Long skuId) {
        cartItemRepository.deleteByUserIdAndSkuId(userId, skuId);
        evictAfterCommit(userId);
    }

    // Call AFTER_COMMIT from CartCleanupListener
    @Transactional
    public void clearCart(Long userId) {
        cartItemRepository.deleteByUserId(userId);
        evictAfterCommit(userId);
    }

    // ---- Guest cart ----

    @Transactional
    public CartItemDto addGuestItem(String guestToken, Long skuId, int quantity) {
        if (guestToken == null || guestToken.isBlank()) {
            throw new InvalidRequestException("guestToken is required");
        }
        if (quantity <= 0) {
            throw new InvalidRequestException(Message.Cart.INVALID_QUANTITY);
        }
        Integer available = inventoryQueryPort.getAvailableQuantity(skuId);
        CartItem item = cartItemRepository.findByGuestTokenAndSkuId(guestToken, skuId).map(existing -> {
            int newTotal = existing.getQuantity() + quantity;
            if (available != null && newTotal > available) {
                throw new ResourceInsufficientException(Message.format(Message.Cart.INSUFFICIENT_STOCK, skuId, available, newTotal));
            }
            existing.setQuantity(newTotal);
            return existing;
        }).orElseGet(() -> {
            if (available != null && quantity > available) {
                throw new ResourceInsufficientException(Message.format(Message.Cart.INSUFFICIENT_STOCK, skuId, available, quantity));
            }
            return CartItem.builder().guestToken(guestToken).skuId(skuId).quantity(quantity).build();
        });
        cartItemRepository.save(item);
        return toDto(item);
    }

    @Transactional
    public void removeGuestItem(String guestToken, Long skuId) {
        cartItemRepository.deleteByGuestTokenAndSkuId(guestToken, skuId);
    }

    @Transactional
    public void clearGuestCart(String guestToken) {
        cartItemRepository.deleteByGuestToken(guestToken);
    }

    /** Gộp giỏ guest vào user sau login: pre-check tồn cho tất cả trước khi gộp. */
    @Transactional
    public void mergeGuestCart(String guestToken, Long userId) {
        List<CartItem> guestItems = cartItemRepository.findByGuestToken(guestToken);
        for (CartItem g : guestItems) {
            Integer available = inventoryQueryPort.getAvailableQuantity(g.getSkuId());
            int inUserCart = cartItemRepository.findByUserIdAndSkuId(userId, g.getSkuId())
                    .map(CartItem::getQuantity).orElse(0);
            if (available != null && inUserCart + g.getQuantity() > available) {
                throw new ResourceInsufficientException(Message.format(
                        Message.Cart.INSUFFICIENT_STOCK, g.getSkuId(), available, inUserCart + g.getQuantity()));
            }
        }
        for (CartItem g : guestItems) {
            cartItemRepository.findByUserIdAndSkuId(userId, g.getSkuId())
                    .ifPresentOrElse(
                            u -> u.setQuantity(u.getQuantity() + g.getQuantity()),
                            () -> cartItemRepository.save(CartItem.builder()
                                    .userId(userId).skuId(g.getSkuId()).quantity(g.getQuantity()).build()));
        }
        cartItemRepository.deleteByGuestToken(guestToken);
        evictAfterCommit(userId);
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
        Runnable evict = () -> {
            try {
                cartCachePort.evict(userId);
            } catch (RuntimeException e) {
                log.warn("Cart cache evict failed for user {}", userId);
            }
        };
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            evict.run();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        evict.run();
                    }
                }
        );
    }
}
