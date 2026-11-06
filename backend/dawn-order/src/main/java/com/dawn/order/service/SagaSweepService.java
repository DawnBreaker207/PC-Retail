package com.dawn.order.service;

import com.dawn.common.contract.InventoryClientPort;
import com.dawn.common.contract.StockDecrementCommand;
import com.dawn.common.lock.AdvisoryLocks;
import com.dawn.order.entity.SubOrder;
import com.dawn.order.entity.enums.SubOrderStatus;
import com.dawn.order.port.out.OrderItemRepository;
import com.dawn.order.port.out.SubOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Detects and resolves SAGA flows stuck in intermediate states.
 * Runs every 60s; uses PostgreSQL advisory lock so only one node executes when scaled out.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SagaSweepService {

    private static final long LOCK_ID = 1002L;
    private static final int TIMEOUT_MINUTES = 30;

    private final AdvisoryLocks advisoryLocks;
    private final SubOrderRepository subOrderRepository;
    private final OrderItemRepository orderItemRepository;
    private final InventoryClientPort inventoryClient;

    @Scheduled(fixedDelay = 60_000)
    public void sweepStuckSagas() {
        advisoryLocks.runIfLeader(LOCK_ID, this::doSweep);
    }

    @Transactional
    public void doSweep() {
        Instant threshold = Instant.now().minus(TIMEOUT_MINUTES, ChronoUnit.MINUTES);

        // Sub-orders stuck in AWAITING_INVENTORY → cancel + release reserved stock
        List<SubOrder> stuckInventory =
                subOrderRepository.findByStatusAndCreatedAtBefore(SubOrderStatus.AWAITING_INVENTORY, threshold);
        for (SubOrder sub : stuckInventory) {
            int rows = subOrderRepository.transitionIfStatus(
                    sub.getId(), SubOrderStatus.AWAITING_INVENTORY, SubOrderStatus.CANCELLED);
            if (rows > 0) {
                releaseInventory(sub);
                log.warn("SAGA sweep: sub-order {} timed out AWAITING_INVENTORY → CANCELLED, inventory released",
                        sub.getId());
            }
        }

        // Sub-orders stuck in PENDING_PAYMENT beyond reservation deadline → expire + release
        // Note: SubOrderCommandService.expiredUnpaidOrders() handles reservedUntil-based expiry;
        // this sweep catches any the scheduled job missed (e.g. node was down).
        List<SubOrder> stuckPayment =
                subOrderRepository.findByStatusAndReservedUntilBefore(SubOrderStatus.PENDING_PAYMENT, Instant.now());
        for (SubOrder sub : stuckPayment) {
            int rows = subOrderRepository.transitionIfStatus(
                    sub.getId(), SubOrderStatus.PENDING_PAYMENT, SubOrderStatus.EXPIRED);
            if (rows > 0) {
                releaseInventory(sub);
                log.warn("SAGA sweep: sub-order {} reservation expired → EXPIRED, inventory released", sub.getId());
            }
        }

        if (!stuckInventory.isEmpty() || !stuckPayment.isEmpty()) {
            log.warn("SAGA sweep complete: {} AWAITING_INVENTORY cancelled, {} PENDING_PAYMENT expired",
                    stuckInventory.size(), stuckPayment.size());
        }
    }

    private void releaseInventory(SubOrder sub) {
        try {
            List<StockDecrementCommand> items = orderItemRepository.findBySubOrderId(sub.getId())
                    .stream()
                    .map(item -> new StockDecrementCommand(item.getSkuId(), item.getQuantity(), sub.getWarehouseId()))
                    .toList();
            if (!items.isEmpty()) {
                inventoryClient.release(items, sub.getOrderId());
            }
        } catch (Exception e) {
            log.error("SAGA sweep: failed to release inventory for sub-order {}: {}", sub.getId(), e.getMessage());
        }
    }
}
