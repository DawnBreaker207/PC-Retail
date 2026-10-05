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
import com.dawn.common.exception.wrapper.InvalidRequestException;
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
    // Upsert 1 câu lệnh: diệt tận gốc race 2 tx cùng tạo dòng (user,sku).
    // Giữ @Retryable cho các race còn lại (version).
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
        log.info("Get existing quantity {}", available);
        int newTotal = cartItemRepository.findByUserIdAndSkuId(userId, skuId)
                .map(CartItem::getQuantity).orElse(0) + quantity;
        log.info("Get existing quantity {} and total in cart {}", available, newTotal);
        if (newTotal > available) {
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

    // ---- Guest cart (khách vãng lai, key bằng guest_token) ----

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

    @Transactional(readOnly = true)
    public List<CartItem> getGuestRawItems(String guestToken) {
        return cartItemRepository.findByGuestToken(guestToken);
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
        // Cache chết thì bỏ qua (log 1 dòng): DB mới là nguồn thật, evict lỗi không được nổ request
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
