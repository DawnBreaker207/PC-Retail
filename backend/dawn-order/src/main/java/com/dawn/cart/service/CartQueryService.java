package com.dawn.cart.service;

import com.dawn.cart.dto.CartItemDto;
import com.dawn.cart.dto.CartSummaryDto;
import com.dawn.cart.entity.CartItem;
import com.dawn.cart.port.in.GetCartUseCase;
import com.dawn.cart.port.out.CartCachePort;
import com.dawn.cart.port.out.CartItemRepository;
import com.dawn.cart.port.out.CartQueryPort;
import com.dawn.common.contract.CatalogQueryPort;
import com.dawn.common.contract.SkuPriceInfo;
import com.dawn.common.exception.wrapper.InvalidRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CartQueryService implements GetCartUseCase, CartQueryPort {

    private final CartItemRepository cartItemRepository;
    private final CartCachePort cartCachePort;
    private final CatalogQueryPort catalogQueryPort;

    @Override
    @Transactional(readOnly = true)
    public CartSummaryDto getCart(Long userId) {
        return cartCachePort.getCart(userId).orElseGet(() -> {
            List<CartItem> items = cartItemRepository.findByUserId(userId);
            CartSummaryDto summary = toEnrichedSummary(items);
            if (!summary.items().isEmpty()) {
                cartCachePort.putCart(userId, summary);
            }
            return summary;
        });
    }

    @Transactional(readOnly = true)
    public CartSummaryDto getGuestCart(String guestToken) {
        if (guestToken == null || guestToken.isBlank()) {
            throw new InvalidRequestException("guestToken is required");
        }
        return toEnrichedSummary(cartItemRepository.findByGuestToken(guestToken));
    }

    @Override
    @Transactional(readOnly = true)
    public int getItemCount(Long userId) {
        return cartCachePort.getItemCount(userId).orElseGet(() -> {
            int count = cartItemRepository.countByUserId(userId);
            cartCachePort.putItemCount(userId, count);
            return count;
        });
    }

    @Override
    @Transactional(readOnly = true)
    public List<CartItemDto> getItems(Long userId) {
        return cartItemRepository.findByUserId(userId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CartItem> getRawItems(Long userId) {
        return cartItemRepository.findByUserId(userId);
    }

    @Transactional(readOnly = true)
    public List<CartItem> getGuestRawItems(String guestToken) {
        return cartItemRepository.findByGuestToken(guestToken);
    }

    // Private Helpers
    private CartSummaryDto toEnrichedSummary(List<CartItem> items) {
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

        return new CartSummaryDto(dtos, dtos.size(), totalAmount);
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
