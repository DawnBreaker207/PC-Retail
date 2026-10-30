package com.dawn.cart.infra.redis;

import com.dawn.cart.dto.CartSummaryDto;
import com.dawn.cart.port.out.CartCachePort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CartCacheAdapter implements CartCachePort {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String CART_KEY = "cart:summary:";
    private static final String COUNT_KEY = "cart:count:";
    private static final Duration TTL_HOURS = Duration.ofHours(24);

    @Override
    public Optional<CartSummaryDto> getCart(Long userId) {
        Object raw = redisTemplate.opsForValue().get(CART_KEY + userId);
        if (raw == null) return Optional.empty();
        return Optional.of(objectMapper.convertValue(raw, CartSummaryDto.class));
    }

    @Override
    public void putCart(Long userId, CartSummaryDto summary) {
        redisTemplate.opsForValue().set(CART_KEY + userId, summary, TTL_HOURS);
    }

    @Override
    public void evict(Long userId) {
        redisTemplate.delete(CART_KEY + userId);
        redisTemplate.delete(COUNT_KEY + userId);
    }

    @Override
    public Optional<Integer> getItemCount(Long userId) {
        Object raw = redisTemplate.opsForValue().get(COUNT_KEY + userId);
        if (raw == null) return Optional.empty();
        if (raw instanceof Integer) return Optional.of((Integer) raw);
        return Optional.of(Integer.parseInt(raw.toString()));
    }

    @Override
    public void putItemCount(Long userId, int count) {
        redisTemplate.opsForValue().set(COUNT_KEY + userId, String.valueOf(count), TTL_HOURS);
    }
}
