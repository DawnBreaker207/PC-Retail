package com.dawn.ai.service;

import com.dawn.common.event.ClickstreamEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Item-based collaborative filtering via co-occurrence matrix.
 * When user views SKU A then B in the same session → increment co(A,B) and co(B,A).
 * Stored in Redis ZSets: rec:item:{skuId} → sorted set of (relatedSkuId, score).
 * Uses userId as session key since ClickstreamEvent has no sessionId.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CooccurrenceService {

    private static final String KEY_PREFIX = "rec:item:";
    private static final Duration TTL = Duration.ofHours(25);

    private final Map<Long, List<Long>> sessionBuffer = new ConcurrentHashMap<>();

    private final StringRedisTemplate redisTemplate;

    public void recordView(ClickstreamEvent event) {
        if (event.skuId() == null || event.userId() == null) return;
        List<Long> views = sessionBuffer.computeIfAbsent(event.userId(), k -> new ArrayList<>());
        if (!views.contains(event.skuId())) {
            for (Long prevSkuId : views) {
                String keyA = KEY_PREFIX + prevSkuId;
                String keyB = KEY_PREFIX + event.skuId();
                redisTemplate.opsForZSet().incrementScore(keyA, event.skuId().toString(), 1.0);
                redisTemplate.opsForZSet().incrementScore(keyB, prevSkuId.toString(), 1.0);
                redisTemplate.expire(keyA, TTL);
                redisTemplate.expire(keyB, TTL);
            }
            views.add(event.skuId());
        }
    }

    public List<Long> getRecommendations(Long skuId, int limit) {
        Set<ZSetOperations.TypedTuple<String>> tuples = redisTemplate.opsForZSet()
                .reverseRangeWithScores(KEY_PREFIX + skuId, 0, limit - 1);
        if (tuples == null) return List.of();
        return tuples.stream()
                .map(t -> Long.parseLong(Objects.requireNonNull(t.getValue())))
                .toList();
    }

    @Scheduled(fixedDelay = 3_600_000)
    public void evictStaleSessionBuffers() {
        if (sessionBuffer.size() > 50_000) {
            sessionBuffer.clear();
            log.info("Cleared co-occurrence session buffer ({} entries evicted)", sessionBuffer.size());
        }
    }
}
