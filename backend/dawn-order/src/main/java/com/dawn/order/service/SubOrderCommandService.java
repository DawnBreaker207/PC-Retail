package com.dawn.order.service;

import com.dawn.common.auditlog.LogConstant;
import com.dawn.common.auditlog.Loggable;
import com.dawn.common.event.SubOrderCancelledEvent;
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
import com.dawn.order.entity.enums.PaymentMethod;
import com.dawn.order.entity.enums.PaymentStatus;
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
            publishCancelledRefundEvent(fresh, target);
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

    /** Publishes refund saga event when a PAID ONLINE sub-order is cancelled or expired. */
    private void publishCancelledRefundEvent(SubOrder sub, SubOrderStatus target) {
        try {
            Order order = orderRepo.findById(sub.getOrderId()).orElse(null);
            if (order == null) return;
            if (order.getPaymentMethod() != PaymentMethod.ONLINE) return;
            if (order.getPaymentStatus() != PaymentStatus.PAID) return;

            String reason = target == SubOrderStatus.EXPIRED ? "expired_unpaid" : "sub_order_cancelled";
            SubOrderCancelledEvent event = new SubOrderCancelledEvent(sub.getId(), sub.getOrderId(), reason);
            String payload = objectMapper.writeValueAsString(event);
            AfterCommit.run(() ->
                    notificationEventPublisher.send("order-events", "CANCEL:" + sub.getId(), payload));
        } catch (Exception e) {
            log.warn("Failed to publish cancelled-refund event for subOrder={}: {}", sub.getId(), e.getMessage());
        }
    }

    private void publishStatusChangedEvent(Long subOrderId, SubOrder sub, SubOrderStatus target) {
        if (target != SubOrderStatus.PAID && target != SubOrderStatus.SHIPPED
                && target != SubOrderStatus.OUT_FOR_DELIVERY && target != SubOrderStatus.DELIVERED
                && target != SubOrderStatus.DELIVERY_FAILED && target != SubOrderStatus.CANCELLED
                && target != SubOrderStatus.RETURNED) {
            return;
        }
        try {
            Order order = orderRepo.findById(sub.getOrderId()).orElse(null);
            if (order == null) return;
            String trackingCode = target == SubOrderStatus.SHIPPED ? sub.getTrackingCode() : null;
            String deliveryNote = target == SubOrderStatus.DELIVERY_FAILED ? sub.getDeliveryNote() : null;
            SubOrderStatusChangedEvent event = new SubOrderStatusChangedEvent(
                    subOrderId, order.getId(), order.getUserId(),
                    order.getGuestEmail(), target.name(), trackingCode, deliveryNote);
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

    @Override
    @Transactional
    public SubOrderDto assignShipper(Long subOrderId, Long shipperId, Long staffId) {
        SubOrder sub = subOrderRepo.findById(subOrderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        Message.format(Message.Order.SUB_ORDER_NOT_FOUND, subOrderId)));
        if (sub.getShipperId() != null) {
            throw new ConflictException("Sub-order already assigned to a shipper");
        }
        sub.setShipperId(shipperId);
        appendHistory(sub, sub.getStatus(), staffId);
        subOrderRepo.save(sub);
        log.info("Sub-order {} assigned to shipper {}", subOrderId, shipperId);
        return toDto(sub);
    }

    @Override
    @Transactional
    public SubOrderDto deliver(Long subOrderId, Long shipperId, boolean codCollected) {
        SubOrder sub = subOrderRepo.findById(subOrderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        Message.format(Message.Order.SUB_ORDER_NOT_FOUND, subOrderId)));
        stateMachine.assertValidTransition(sub.getStatus(), SubOrderStatus.DELIVERED);
        int claimed = subOrderRepo.transitionIfStatus(subOrderId, sub.getStatus(), SubOrderStatus.DELIVERED);
        if (claimed == 0) throw new ConflictException("Concurrent status change, please retry");

        SubOrder fresh = subOrderRepo.findById(subOrderId).orElse(sub);
        if (codCollected) {
            fresh.setCodCollectedAt(Instant.now());
            // mark order as paid for COD
            orderRepo.findById(fresh.getOrderId()).ifPresent(o -> {
                o.setPaymentStatus(com.dawn.order.entity.enums.PaymentStatus.PAID);
                orderRepo.save(o);
            });
        }
        appendHistory(fresh, SubOrderStatus.DELIVERED, shipperId);
        fresh.setStatus(SubOrderStatus.DELIVERED);
        subOrderRepo.save(fresh);
        publishStatusChangedEvent(subOrderId, fresh, SubOrderStatus.DELIVERED);
        List<OrderItem> items = orderItemRepo.findBySubOrderId(subOrderId);
        eventPublisher.publishEvent(new SubOrderDeliveredEvent(subOrderId, fresh.getOrderId(), items));
        return toDto(fresh);
    }

    private static final int MAX_DELIVERY_ATTEMPTS = 2;

    @Override
    @Transactional
    public SubOrderDto deliveryFailed(Long subOrderId, Long shipperId, String note) {
        SubOrder sub = subOrderRepo.findById(subOrderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        Message.format(Message.Order.SUB_ORDER_NOT_FOUND, subOrderId)));
        stateMachine.assertValidTransition(sub.getStatus(), SubOrderStatus.DELIVERY_FAILED);
        int claimed = subOrderRepo.transitionIfStatus(subOrderId, sub.getStatus(), SubOrderStatus.DELIVERY_FAILED);
        if (claimed == 0) throw new ConflictException("Concurrent status change, please retry");

        SubOrder fresh = subOrderRepo.findById(subOrderId).orElse(sub);
        fresh.setDeliveryNote(note);
        fresh.setDeliveryAttemptedAt(Instant.now());
        fresh.setDeliveryAttempts(fresh.getDeliveryAttempts() + 1);
        appendHistory(fresh, SubOrderStatus.DELIVERY_FAILED, shipperId);
        fresh.setStatus(SubOrderStatus.DELIVERY_FAILED);
        subOrderRepo.save(fresh);
        publishStatusChangedEvent(subOrderId, fresh, SubOrderStatus.DELIVERY_FAILED);

        if (fresh.getDeliveryAttempts() >= MAX_DELIVERY_ATTEMPTS) {
            log.info("Sub-order {} reached max delivery attempts ({}), auto-cancelling", subOrderId, MAX_DELIVERY_ATTEMPTS);
            transition(subOrderId, SubOrderStatus.CANCELLED, shipperId);
            // re-fetch so response reflects auto-cancel, not stale DELIVERY_FAILED
            return toDto(subOrderRepo.findById(subOrderId).orElse(fresh));
        }

        return toDto(fresh);
    }

    private SubOrderDto toDto(SubOrder s) {
        return new SubOrderDto(
                s.getId(),
                s.getOrderId(),
                s.getFulfillmentType(),
                s.getSubtotal(),
                s.getShippingFee(),
                s.getDiscountAmount(),
                s.getFinalAmount(),
                s.getStatus(),
                s.getShippingMethod(),
                s.getShipperId(),
                s.getDeliveryAttemptedAt(),
                s.getCodCollectedAt(),
                List.of());
    }
}
