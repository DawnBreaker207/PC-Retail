package com.dawn.cart.infra.bridge;

import com.dawn.cart.port.out.CartCachePort;
import com.dawn.cart.port.out.CartItemRepository;
import com.dawn.common.contract.CartItemSnapshot;
import com.dawn.common.contract.CartClientPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CartClientAdapter implements CartClientPort {

    private static final String CART_KEY = "cart:";

    private final CartItemRepository cartItemRepository;
    private final CartCachePort cartCache;

    @Override
    public List<CartItemSnapshot> getItems(Long userId) {
        return cartItemRepository.findByUserId(userId)
                .stream()
                .map(item -> new CartItemSnapshot(item.getSkuId(), item.getQuantity()))
                .toList();
    }

    @Override
    public void clearCart(Long userId) {
        cartItemRepository.deleteByUserId(userId);
        cartCache.evict(userId);
    }

    @Override
    public List<CartItemSnapshot> getGuestItems(String guestToken) {
        return cartItemRepository.findByGuestToken(guestToken)
                .stream()
                .map(item -> new CartItemSnapshot(item.getSkuId(), item.getQuantity()))
                .toList();
    }

    @Override
    public void clearGuestCart(String guestToken) {
        cartItemRepository.deleteByGuestToken(guestToken);
    }
}
