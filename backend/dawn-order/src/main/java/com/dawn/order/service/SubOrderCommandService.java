package com.dawn.order.service;

import com.dawn.common.auditlog.LogConstant;
import com.dawn.common.auditlog.Loggable;
import com.dawn.common.event.SubOrderStatusChangedEvent;
import com.dawn.common.exception.wrapper.ConflictException;
import com.dawn.common.exception.wrapper.ResourceNotFoundException;
import com.dawn.common.message.Message;
import com.dawn.common.redis.service.RedisService;
import com.dawn.common.contract.StockDecrementCommand;
import com.dawn.common.util.AfterCommit;
import com.dawn.order.dto.SubOrderDto;
import com.dawn.order.entity.Order;
import com.dawn.order.entity.OrderItem;
import com.dawn.order.entity.SubOrder;
import com.dawn.order.entity.enums.SubOrderStatus;
import com.dawn.order.event.SubOrderDeliveredEvent;
import com.dawn.order.infra.kafka.NotificationEventPublisher;
import com.dawn.order.port.in.TransitionSubOrderUseCase;
import com.dawn.common.contract.InventoryClientPort;
import com.dawn.common.contract.GuestRiskPort;
import com.dawn.order.port.out.OrderItemRepository;
import com.dawn.order.port.out.OrderRepository;
import com.dawn.order.port.out.SubOrderRepository;
import com.dawn.order.statemachine.SubOrderStateMachine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubOrderCommandService implements TransitionSubOrderUseCase {

    private final SubOrderRepository subOrderRepo;
    private final OrderRepository orderRepo;
    private final OrderItemRepository orderItemRepo;
    private final SubOrderStateMachine stateMachine;
    private final InventoryClientPort inventoryClient;
    private final GuestRiskPort guestRiskPort;
    private final RedisService redisService;
    private final ApplicationEventPublisher eventPublisher;
    private final NotificationEventPublisher notificationEventPublisher;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    @Loggable(action = LogConstant.Action.TRANSITION_ORDER, entity = LogConstant.Entity.SUB_ORDER)
    public SubOrderDto transition(Long subOrderId, SubOrderStatus target, Long staffId) {
        SubOrder subOrder = subOrderRepo
                .findById(subOrderId)
                .orElseThrow(() -> new ResourceNotFoundException(Message.format(Message.Order.SUB_ORDER_NOT_FOUND, subOrderId)));


        stateMachine.assertValidTransition(subOrder.getStatus(), target);

        // CAS before inventory call; losing the race throws immediately, avoiding duplicate inventory calls
        int claimed = subOrderRepo.transitionIfStatus(subOrderId, subOrder.getStatus(), target);
        if (claimed == 0) {
            throw new ConflictException(Message.format(Message.Order.INVALID_TRANSITION, "STALE", target));
        }

        try {
            if (target == SubOrderStatus.PAID) {
                List<StockDecrementCommand> items = toStockCommands(subOrder.getId());
                inventoryClient.deduct(items, subOrder.getOrderId());
            }
            if (target == SubOrderStatus.CANCELLED || target == SubOrderStatus.EXPIRED) {
                List<StockDecrementCommand> items = toStockCommands(subOrder.getId());
                if (subOrder.getStatus() == SubOrderStatus.PENDING_PAYMENT) {
                    // stock not yet decremented; release reservation only
                    inventoryClient.release(items, subOrder.getOrderId());
                } else {
                    // stock already decremented (from PAID/PROCESSING); restock to compensate
                    inventoryClient.restock(items, staffId);
                }
            }
            if (target == SubOrderStatus.RETURNED) {
                // physical item back in warehouse — restock confirmed by staff
                List<StockDecrementCommand> items = toStockCommands(subOrder.getId());
                inventoryClient.restock(items, staffId);
            }
        } catch (RuntimeException e) {
            // roll back CAS claim if inventory call fails
            subOrderRepo.transitionIfStatus(subOrderId, target, subOrder.getStatus());
            throw e;
        }

        SubOrder fresh = subOrderRepo.findById(subOrderId).orElse(subOrder);
        appendHistory(fresh, target, staffId);
        fresh.setStatus(target);
        subOrderRepo.save(fresh);
        if (target == SubOrderStatus.CANCELLED || target == SubOrderStatus.EXPIRED) {
            recordGuestAbuse(subOrder.getOrderId());
        }
        publishStatusChangedEvent(subOrderId, fresh, target);
        if (target == SubOrderStatus.DELIVERED) {
            List<OrderItem> items = orderItemRepo.findBySubOrderId(subOrderId);
            eventPublisher.publishEvent(new SubOrderDeliveredEvent(subOrderId, fresh.getOrderId(), items));
        }
        return toDto(fresh);
    }

    @Override
    @Transactional
    public SubOrderDto cancel(Long subOrderId, Long userId) {
        // cancel delegates to transition to avoid duplicated logic
        return transition(subOrderId, SubOrderStatus.CANCELLED, userId);
    }

    @Override
    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void expiredUnpaidOrders() {
        Instant now = Instant.now();
        List<SubOrder> expired = new java.util.ArrayList<>(
                subOrderRepo.findByStatusAndReservedUntilBefore(SubOrderStatus.PENDING_PAYMENT, now));
        // SAGA Stuck Order Detection: Auto-expire orders stuck in PENDING_PAYMENT over 30 mins
        List<SubOrder> stuckOrders = subOrderRepo.findByStatusAndCreatedAtBefore(
                SubOrderStatus.PENDING_PAYMENT, now.minus(Duration.ofMinutes(30)));
        for (SubOrder so : stuckOrders) {
            if (expired.stream().noneMatch(e -> e.getId().equals(so.getId()))) {
                expired.add(so);
            }
        }

        List<Long> subOrderIds = expired.stream().map(SubOrder::getId).toList();
        Map<Long, List<StockDecrementCommand>> itemsMap = orderItemRepo.findBySubOrderIdIn(subOrderIds)
                .stream()
                .collect(Collectors.groupingBy(
                        OrderItem::getSubOrderId,
                        Collectors.mapping(
                                item -> new StockDecrementCommand(item.getSkuId(), item.getQuantity()),
                                Collectors.toList())));

        for (SubOrder subOrder : expired) {
            String lockKey = "lock:expire:so:" + subOrder.getId();
            Boolean locked = redisService.setIfAbsent(lockKey, "1", Duration.ofSeconds(30));
            if (Boolean.FALSE.equals(locked)) continue;


            // CAS PENDING_PAYMENT→EXPIRED; already-PAID orders are skipped
            int claimed = subOrderRepo.transitionIfStatus(
                    subOrder.getId(), SubOrderStatus.PENDING_PAYMENT, SubOrderStatus.EXPIRED);
            if (claimed == 0) continue;

            List<StockDecrementCommand> items = itemsMap.getOrDefault(subOrder.getId(), List.of());
            inventoryClient.release(items, subOrder.getOrderId());
            recordGuestAbuse(subOrder.getOrderId());

            SubOrder fresh = subOrderRepo.findById(subOrder.getId()).orElse(subOrder);
            appendHistory(fresh, SubOrderStatus.EXPIRED, null);
            fresh.setStatus(SubOrderStatus.EXPIRED);
            subOrderRepo.save(fresh);
        }
    }

    private List<StockDecrementCommand> toStockCommands(Long subOrderId) {
        Long warehouseId = subOrderRepo.findById(subOrderId)
                .map(com.dawn.order.entity.SubOrder::getWarehouseId).orElse(null);
        return orderItemRepo.findBySubOrderId(subOrderId)
                .stream()
                .map(item -> new StockDecrementCommand(item.getSkuId(), item.getQuantity(), warehouseId))
                .toList();
    }

    private void publishStatusChangedEvent(Long subOrderId, SubOrder sub, SubOrderStatus target) {
        if (target != SubOrderStatus.PAID && target != SubOrderStatus.SHIPPED && target != SubOrderStatus.DELIVERED) {
            return;
        }
        try {
            Order order = orderRepo.findById(sub.getOrderId()).orElse(null);
            if (order == null) return;
            String trackingCode = target == SubOrderStatus.SHIPPED ? sub.getTrackingCode() : null;
            SubOrderStatusChangedEvent event = new SubOrderStatusChangedEvent(
                    subOrderId, order.getId(), order.getUserId(),
                    order.getGuestEmail(), target.name(), trackingCode);
            String payload = objectMapper.writeValueAsString(event);
            // fire-and-forget after commit: notification loss is acceptable vs. risking the status transition
            AfterCommit.run(() ->
                    notificationEventPublisher.send("order-events", "SUB_ORDER:" + subOrderId, payload));
        } catch (Exception e) {
            log.warn("Failed to build notification event for subOrder={} status={}: {}",
                    subOrderId, target, e.getMessage());
        }
    }

    // record phone abuse for guest orders that are cancelled, expired, or payment-failed
    private void recordGuestAbuse(Long orderId) {
        try {
            orderRepo.findById(orderId).ifPresent(o -> {
                if (o.getUserId() == null && o.getGuestPhone() != null) {
                    guestRiskPort.recordPhoneAbuse(o.getGuestPhone());
                }
            });
        } catch (RuntimeException e) {
            // abuse recording must not break the main flow
            log.warn("Record phone abuse failed for order {}", orderId);
        }
    }

    private void appendHistory(SubOrder subOrder, SubOrderStatus to, Long actorId) {        subOrder.getStatusHistory().add(Map.of(
                "from", subOrder.getStatus().name(),
                "to", to.name(),
                "actorId", actorId == null ? "SYSTEM" : actorId.toString(),
                "createdAt", Instant.now().toString()
        ));
    }

    private SubOrderDto toDto(SubOrder s) {
        return new SubOrderDto(
                s.getId(),
                s.getFulfillmentType(),
                s.getSubtotal(),
                s.getShippingFee(),
                s.getDiscountAmount(),
                s.getFinalAmount(),
                s.getStatus(),
                List.of());
    }
}
