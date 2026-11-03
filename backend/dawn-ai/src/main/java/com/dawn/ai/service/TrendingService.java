package com.dawn.ai.service;

import com.dawn.common.event.ClickstreamEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;

/**
 * Trending products — behavioral signals from clickstream + purchase events.
 *
 * Redis sorted sets (decay window: configurable, default 24h):
 *   trending:views:24h   → ZINCRBY score=1 on each view
 *   trending:carts:24h   → ZINCRBY score=2 on add-to-cart
 *   trending:buys:24h    → ZINCRBY score=5 on purchase
 *
 * Combined score = views*1 + carts*2 + buys*5 → ZREVRANGE for top-N.
 * Keys expire after 48h. A nightly job resets them to avoid unbounded growth.
 *
 * Fallback when Redis is unavailable: DB count from order_items in last 24h.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TrendingService {

    private static final String KEY_VIEWS = "trending:views:24h";
    private static final String KEY_CARTS = "trending:carts:24h";
    private static final String KEY_BUYS  = "trending:buys:24h";
    private static final Duration KEY_TTL = Duration.ofHours(48);

    private static final double VIEW_W  = 1.0;
    private static final double CART_W  = 2.0;
    private static final double BUY_W   = 5.0;

    private final StringRedisTemplate redis;
    private final JdbcTemplate jdbcTemplate;

    public void recordView(ClickstreamEvent event) {
        if (event.skuId() == null) return;
        try {
            redis.opsForZSet().incrementScore(KEY_VIEWS, event.skuId().toString(), VIEW_W);
            redis.expire(KEY_VIEWS, KEY_TTL);
        } catch (Exception e) {
            log.debug("Redis trending view record failed: {}", e.getMessage());
        }
    }

    public void recordAddToCart(Long skuId) {
        if (skuId == null) return;
        try {
            redis.opsForZSet().incrementScore(KEY_CARTS, skuId.toString(), CART_W);
            redis.expire(KEY_CARTS, KEY_TTL);
        } catch (Exception e) {
            log.debug("Redis trending cart record failed: {}", e.getMessage());
        }
    }

    public void recordPurchase(Long skuId, int quantity) {
        if (skuId == null) return;
        try {
            redis.opsForZSet().incrementScore(KEY_BUYS, skuId.toString(), BUY_W * quantity);
            redis.expire(KEY_BUYS, KEY_TTL);
        } catch (Exception e) {
            log.debug("Redis trending purchase record failed: {}", e.getMessage());
        }
    }

    public List<Long> getTopTrending(int limit) {
        int safeLimit = Math.min(Math.max(limit, 1), 50);
        try {
            return getTopTrendingFromRedis(safeLimit);
        } catch (Exception e) {
            log.warn("Redis unavailable for trending, falling back to DB: {}", e.getMessage());
            return getTopTrendingFromDb(safeLimit);
        }
    }

    private List<Long> getTopTrendingFromRedis(int limit) {
        // Merge view + cart + buy scores into a combined map
        Map<String, Double> combined = new LinkedHashMap<>();

        mergeZSet(KEY_VIEWS, combined, VIEW_W);
        mergeZSet(KEY_CARTS, combined, CART_W);
        mergeZSet(KEY_BUYS, combined, BUY_W);

        if (combined.isEmpty()) return getTopTrendingFromDb(limit);

        return combined.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(limit)
                .map(e -> Long.parseLong(e.getKey()))
                .toList();
    }

    private void mergeZSet(String key, Map<String, Double> combined, double weight) {
        Set<org.springframework.data.redis.core.ZSetOperations.TypedTuple<String>> tuples =
                redis.opsForZSet().reverseRangeWithScores(key, 0, -1);
        if (tuples == null) return;
        for (var tuple : tuples) {
            if (tuple.getValue() != null && tuple.getScore() != null) {
                combined.merge(tuple.getValue(), tuple.getScore() * weight, Double::sum);
            }
        }
    }

    private List<Long> getTopTrendingFromDb(int limit) {
        return jdbcTemplate.queryForList("""
                SELECT oi.sku_id
                FROM order_item oi
                JOIN sub_order so ON so.id = oi.sub_order_id
                WHERE so.created_at > NOW() - INTERVAL '24 hours'
                  AND so.status IN ('CONFIRMED', 'DELIVERED', 'COMPLETED')
                GROUP BY oi.sku_id
                ORDER BY SUM(oi.quantity) DESC
                LIMIT ?
                """, Long.class, limit);
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void resetDailyCounters() {
        try {
            redis.delete(List.of(KEY_VIEWS, KEY_CARTS, KEY_BUYS));
            log.info("Reset trending counters");
        } catch (Exception e) {
            log.warn("Failed to reset trending counters: {}", e.getMessage());
        }
    }
}
