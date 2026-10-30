package com.dawn.ai.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FraudScorerService {

    private final StringRedisTemplate redisTemplate;
    private final JdbcTemplate jdbcTemplate;

    public FraudResult score(Long userId, String shippingAddress, BigDecimal totalAmount, int itemCount) {
        int score = 0;
        List<String> reasons = new ArrayList<>();

        if (itemCount > 5) {
            score += 30;
            reasons.add("Large item count: " + itemCount);
        }

        int hour = LocalTime.now().getHour();
        if (hour >= 1 && hour <= 5) {
            score += 10;
            reasons.add("Unusual hour: " + hour + ":00");
        }

        String velocityKey = "fraud:velocity:" + userId;
        Long count = redisTemplate.opsForValue().increment(velocityKey);
        redisTemplate.expire(velocityKey, Duration.ofMinutes(10));
        if (count != null && count > 3) {
            score += 50;
            reasons.add("High velocity: " + count + " orders in 10min");
        }

        double zScore = calculateZScore(userId, totalAmount);
        if (zScore > 3.0) {
            score += 30;
            reasons.add("Abnormal amount (z=" + String.format("%.1f", zScore) + ")");
        }

        FraudLevel level = score < 30 ? FraudLevel.OK
                : score < 70 ? FraudLevel.MANUAL_REVIEW
                : FraudLevel.BLOCK;

        log.debug("Fraud score for user {}: {} — {}", userId, score, level);
        return new FraudResult(score, reasons, level);
    }

    private double calculateZScore(Long userId, BigDecimal amount) {
        try {
            var stats = jdbcTemplate.queryForMap(
                    "SELECT AVG(total_amount) as avg, STDDEV(total_amount) as std FROM orders WHERE user_id = ? AND created_at > NOW() - INTERVAL '90 days'",
                    userId);
            if (stats.get("avg") == null || stats.get("std") == null) return 0.0;
            double avg = ((Number) stats.get("avg")).doubleValue();
            double std = ((Number) stats.get("std")).doubleValue();
            if (std == 0) return 0.0;
            return Math.abs((amount.doubleValue() - avg) / std);
        } catch (Exception e) {
            return 0.0;
        }
    }

    public enum FraudLevel { OK, MANUAL_REVIEW, BLOCK }

    public record FraudResult(int score, List<String> reasons, FraudLevel level) {}
}
