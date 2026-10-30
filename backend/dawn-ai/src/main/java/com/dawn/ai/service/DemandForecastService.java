package com.dawn.ai.service;

import com.dawn.common.lock.AdvisoryLocks;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Exponential Weighted Moving Average demand forecasting.
 * Runs nightly at 2am — forecasts demand per SKU for next 7 days.
 * Publishes to low-stock-events when forecast exceeds current stock.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DemandForecastService {

    private static final double ALPHA = 0.3;
    private static final long FORECAST_LOCK = 1003L;

    private final JdbcTemplate jdbcTemplate;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final AdvisoryLocks advisoryLocks;
    private final ObjectMapper objectMapper;

    @Scheduled(cron = "0 0 2 * * *")
    public void runForecast() {
        advisoryLocks.runIfLeader(FORECAST_LOCK, this::doForecast);
    }

    private void doForecast() {
        log.info("Starting EWMA demand forecast batch");
        List<Map<String, Object>> skuDemands = jdbcTemplate.queryForList("""
                SELECT oi.sku_id,
                       DATE(o.created_at) AS sale_date,
                       SUM(oi.quantity) AS daily_qty
                FROM order_items oi
                JOIN orders o ON o.id = oi.order_id
                WHERE o.status IN ('CONFIRMED', 'DELIVERED')
                  AND o.created_at >= NOW() - INTERVAL '30 days'
                GROUP BY oi.sku_id, DATE(o.created_at)
                ORDER BY oi.sku_id, sale_date
                """);

        TreeMap<Long, List<Double>> bySkuId = new TreeMap<>();
        for (var row : skuDemands) {
            Long skuId = ((Number) row.get("sku_id")).longValue();
            double qty = ((Number) row.get("daily_qty")).doubleValue();
            bySkuId.computeIfAbsent(skuId, k -> new ArrayList<>()).add(qty);
        }

        AtomicInteger alerts = new AtomicInteger();
        for (var entry : bySkuId.entrySet()) {
            Long skuId = entry.getKey();
            double forecastDaily = ewma(entry.getValue());
            double needed7Days = forecastDaily * 7;

            Integer stock = jdbcTemplate.queryForObject(
                    "SELECT COALESCE(SUM(available_quantity), 0) FROM inventory_items WHERE sku_id = ?",
                    Integer.class, skuId);
            int currentStock = stock == null ? 0 : stock;

            if (currentStock < needed7Days) {
                publishLowStockAlert(skuId, currentStock, (int) Math.ceil(needed7Days));
                alerts.incrementAndGet();
            }
        }
        log.info("Demand forecast complete: {} SKUs processed, {} low-stock alerts", bySkuId.size(), alerts.get());
    }

    private double ewma(List<Double> series) {
        if (series.isEmpty()) return 0;
        double ewma = series.get(0);
        for (int i = 1; i < series.size(); i++) {
            ewma = ALPHA * series.get(i) + (1 - ALPHA) * ewma;
        }
        return ewma;
    }

    private void publishLowStockAlert(Long skuId, int currentStock, int forecastNeeded) {
        try {
            String payload = objectMapper.writeValueAsString(Map.of(
                    "skuId", skuId,
                    "currentStock", currentStock,
                    "forecastNeeded7Days", forecastNeeded,
                    "alertType", "LOW_STOCK_FORECAST"
            ));
            kafkaTemplate.send("low-stock-events", skuId.toString(), payload);
        } catch (Exception e) {
            log.error("Failed to publish low-stock alert for sku {}", skuId, e);
        }
    }
}
