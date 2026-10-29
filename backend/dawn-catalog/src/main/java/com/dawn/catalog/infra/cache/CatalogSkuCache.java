package com.dawn.catalog.infra.cache;

import com.dawn.catalog.dto.SkuPriceInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Cache kết quả tra SKU trên Redis tự build: chỉ dùng SET/GET + PX.
 * Lỗi cache luôn fallback DB (không ném ra ngoài).
 */
@Component
@Slf4j
@ConditionalOnProperty(name = "app.cache.custom-redis.enabled", havingValue = "true")
public class CatalogSkuCache {

    private static final Duration TTL = Duration.ofMinutes(5);
    private static final String PREFIX = "catalog:sku-details:";

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    public CatalogSkuCache(@Qualifier("customRedisTemplate") RedisTemplate<String, String> redisTemplate,
                           ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public Optional<Map<Long, SkuPriceInfo>> get(List<Long> skuIds) {
        try {
            String json = redisTemplate.opsForValue().get(key(skuIds));
            if (json == null) {
                return Optional.empty();
            }
            return Optional.of(objectMapper.readValue(json, new TypeReference<Map<Long, SkuPriceInfo>>() {
            }));
        } catch (RuntimeException e) {
            log.debug("Custom Redis GET failed, fallback DB: {}", e.getMessage());
            return Optional.empty();
        }
    }

    public void put(List<Long> skuIds, Map<Long, SkuPriceInfo> details) {
        try {
            redisTemplate.opsForValue().set(key(skuIds), objectMapper.writeValueAsString(details), TTL);
        } catch (RuntimeException e) {
            log.debug("Custom Redis SET failed: {}", e.getMessage());
        }
    }

    private String key(List<Long> skuIds) {
        return PREFIX + skuIds.stream().sorted().map(String::valueOf).collect(Collectors.joining(","));
    }
}
