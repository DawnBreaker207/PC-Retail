package com.dawn.inventory.infra.scheduler;

import com.dawn.common.redis.service.RedisService;
import com.dawn.inventory.entity.Inventory;
import com.dawn.inventory.infra.persistence.JpaInventoryRepository;
import com.dawn.inventory.port.out.InventoryCachePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.UUID;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryReconciliationJob {
    private static final String LOCK_KEY = "lock:reconciliation";
    private static final Duration LOCK_TTL = Duration.ofMinutes(10);

    private final JpaInventoryRepository inventoryRepository;
    private final InventoryCachePort cachePort;
    private final RedisService redisService;

    @Scheduled(fixedDelay = 5 * 60 * 1000)
    @Transactional(readOnly = true)
    public void sync() {
        String instanceId = UUID.randomUUID().toString();
        Boolean acquired = redisService.setIfAbsent(LOCK_KEY, instanceId, LOCK_TTL);
        if (Boolean.FALSE.equals(acquired)) {
            log.warn("Previous reconciliation still running, skipping this execution");
            return;
        }
        try {
            log.info("Inventory reconciliation started");
            try (Stream<Inventory> stream = inventoryRepository.streamAll()) {
                stream.forEach(inv -> cachePort.syncFromDb(inv.getWarehouseId(), inv.getSkuId(), inv.getAvailableQuantity()));
            }
            log.info("Inventory reconciliation completed");
        } finally {
            String currentValue = (String) redisService.get(LOCK_KEY);
            if (instanceId.equals(currentValue)) {
                redisService.delete(LOCK_KEY);
            }
        }
    }
}
