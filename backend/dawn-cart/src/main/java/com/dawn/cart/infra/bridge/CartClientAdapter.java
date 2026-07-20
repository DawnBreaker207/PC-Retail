package com.dawn.cart.infra.bridge;

import com.dawn.cart.port.out.CartCachePort;
import com.dawn.cart.port.out.CartItemRepository;
import com.dawn.order.dto.CartItemSnapshot;
import com.dawn.order.port.out.CartClientPort;
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
}
