package com.dawn.common.bloom;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * Client-side bloom filter backed by Redis BITFIELD.
 * Uses 3 hash functions on a 1M-bit field (~128KB per key).
 * False positive rate ~0.6% at 100k elements; false negative = 0.
 */
@Component
@RequiredArgsConstructor
public class RedisBloomFilter {

    private static final long BIT_SIZE = 1L << 20; // 1M bits
    private static final int HASH_COUNT = 3;

    private final StringRedisTemplate redisTemplate;

    public void add(String filterKey, String element) {
        for (int i = 0; i < HASH_COUNT; i++) {
            redisTemplate.opsForValue().setBit(filterKey, hash(element, i), true);
        }
    }

    public boolean mightContain(String filterKey, String element) {
        for (int i = 0; i < HASH_COUNT; i++) {
            Boolean set = redisTemplate.opsForValue().getBit(filterKey, hash(element, i));
            if (!Boolean.TRUE.equals(set)) return false;
        }
        return true;
    }

    public void expire(String filterKey, Duration ttl) {
        redisTemplate.expire(filterKey, ttl);
    }

    private long hash(String element, int seed) {
        byte[] bytes = (element + seed).getBytes(StandardCharsets.UTF_8);
        long h = 0;
        for (byte b : bytes) {
            h = h * 31 + b;
        }
        return Math.abs(h) % BIT_SIZE;
    }
}
