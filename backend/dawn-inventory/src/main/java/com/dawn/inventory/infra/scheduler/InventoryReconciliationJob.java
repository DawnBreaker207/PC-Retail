package com.dawn.inventory.infra.scheduler;

import com.dawn.inventory.infra.persistence.JpaInventoryRepository;
import com.dawn.inventory.port.out.InventoryCachePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryReconciliationJob {
    private final JpaInventoryRepository inventoryRepository;
    private final InventoryCachePort cachePort;

    @Scheduled(fixedDelay = 5 * 60 * 1000)
    @Transactional(readOnly = true)
    public void sync() {
        log.info("Inventory reconciliation started");
        inventoryRepository.findAll().forEach(inv -> cachePort.syncFromDb(inv.getSkuId(), inv.getAvailableQuantity()));
        log.info("Inventory reconciliation completed");
    }

}
