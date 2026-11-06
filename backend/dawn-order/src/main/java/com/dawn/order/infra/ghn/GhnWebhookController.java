package com.dawn.order.infra.ghn;

import com.dawn.order.entity.SubOrder;
import com.dawn.order.entity.enums.SubOrderStatus;
import com.dawn.order.port.out.SubOrderRepository;
import com.dawn.order.statemachine.SubOrderStateMachine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Receives real-time delivery status updates from GHN (Giao Hàng Nhanh).
 * GHN sends POST callbacks to this endpoint when shipment status changes.
 * No auth header required by GHN; we validate by looking up trackingCode.
 */
@RestController
@RequestMapping("/webhooks/ghn")
@RequiredArgsConstructor
@Slf4j
public class GhnWebhookController {

    private final SubOrderRepository subOrderRepository;
    private final SubOrderStateMachine stateMachine;

    @PostMapping
    @Transactional
    public void onWebhook(@RequestBody Map<String, Object> payload) {
        String orderCode = (String) payload.get("OrderCode");
        String status = (String) payload.get("Status");

        if (orderCode == null || status == null) {
            log.warn("GHN webhook missing OrderCode or Status: {}", payload);
            return;
        }

        SubOrderStatus target = mapGhnStatus(status);
        if (target == null) {
            log.debug("GHN webhook status '{}' has no mapping, skipping (order={})", status, orderCode);
            return;
        }

        List<SubOrder> matches = subOrderRepository.findByTrackingCode(orderCode);
        if (matches.isEmpty()) {
            log.warn("GHN webhook: no SubOrder found with trackingCode={}", orderCode);
            return;
        }

        for (SubOrder sub : matches) {
            try {
                stateMachine.assertValidTransition(sub.getStatus(), target);
                int rows = subOrderRepository.transitionIfStatus(sub.getId(), sub.getStatus(), target);
                if (rows > 0) {
                    SubOrder fresh = subOrderRepository.findById(sub.getId()).orElse(sub);
                    fresh.getStatusHistory().add(Map.of(
                            "from", sub.getStatus().name(),
                            "to", target.name(),
                            "source", "GHN_WEBHOOK",
                            "ghnStatus", status,
                            "createdAt", Instant.now().toString()));
                    fresh.setWebhookLog(Map.of(
                            "lastGhnStatus", status,
                            "receivedAt", Instant.now().toString(),
                            "orderCode", orderCode));
                    subOrderRepository.save(fresh);
                    log.info("GHN webhook: SubOrder {} transitioned {} → {} (ghn={})",
                            sub.getId(), sub.getStatus(), target, status);
                }
            } catch (Exception e) {
                log.warn("GHN webhook: could not transition SubOrder {} to {}: {}",
                        sub.getId(), target, e.getMessage());
            }
        }
    }

    /** Maps GHN shipment status codes to internal SubOrderStatus. */
    private SubOrderStatus mapGhnStatus(String ghnStatus) {
        return switch (ghnStatus.toLowerCase()) {
            case "delivering", "on_delivery" -> SubOrderStatus.SHIPPED;
            case "delivered" -> SubOrderStatus.DELIVERED;
            case "cancel" -> SubOrderStatus.CANCELLED;
            case "return", "returned", "return_transporting", "return_sorting",
                    "waiting_to_return" -> SubOrderStatus.RETURN_REQUESTED;
            default -> null;
        };
    }
}
