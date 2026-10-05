package com.dawn.inventory.infra.scheduler;

import com.dawn.inventory.infra.persistence.JpaInventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

// docs: cron low-stock → push Notification (tránh spam bằng check chưa đọc).
// ponytail: JDBC trực tiếp thay dep dawn-engagement
@Component
@RequiredArgsConstructor
@Slf4j
public class LowStockAlertJob {

    private final JpaInventoryRepository inventoryRepo;
    private final JdbcTemplate jdbc;

    @Scheduled(fixedDelay = 15 * 60 * 1000)
    @Transactional
    public void alert() {
        var low = inventoryRepo.findAll().stream()
                .filter(i -> i.getStockQuantity() - i.getReservedQuantity() <= 5)
                .toList();
        for (var inv : low) {
            Integer exists = jdbc.queryForObject(
                    "SELECT COUNT(*) FROM notification WHERE user_id=1 AND type='LOW_STOCK' AND reference_id=? AND is_read=false",
                    Integer.class, inv.getSkuId());
            if (exists != null && exists > 0) continue;
            jdbc.update(
                    "INSERT INTO notification (user_id,type,title,body,reference_type,reference_id,is_read) VALUES (1,'LOW_STOCK',?,?, 'SKU',?,false)",
                    "Sắp hết hàng SKU " + inv.getSkuId(), "Tồn khả dụng <= 5", inv.getSkuId());
            log.info("Low stock alert sku={}", inv.getSkuId());
        }
    }
}
