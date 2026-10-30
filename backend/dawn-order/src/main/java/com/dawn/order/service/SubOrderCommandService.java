package com.dawn.order.service;

import com.dawn.common.auditlog.LogConstant;
import com.dawn.common.auditlog.Loggable;
import com.dawn.common.exception.wrapper.ConflictException;
import com.dawn.common.exception.wrapper.ResourceNotFoundException;
import com.dawn.common.message.Message;
import com.dawn.common.redis.service.RedisService;
import com.dawn.common.contract.StockDecrementCommand;
import com.dawn.order.dto.SubOrderDto;
import com.dawn.order.entity.OrderItem;
import com.dawn.order.entity.SubOrder;
import com.dawn.order.entity.enums.SubOrderStatus;
import com.dawn.order.port.in.TransitionSubOrderUseCase;
import com.dawn.common.contract.InventoryClientPort;
import com.dawn.common.contract.GuestRiskPort;
import com.dawn.order.port.out.OrderItemRepository;
import com.dawn.order.port.out.OrderRepository;
import com.dawn.order.port.out.SubOrderRepository;
import com.dawn.order.statemachine.SubOrderStateMachine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Override
    @Transactional
    @Loggable(action = LogConstant.Action.TRANSITION_ORDER, entity = LogConstant.Entity.SUB_ORDER)
    public SubOrderDto transition(Long subOrderId, SubOrderStatus target, Long staffId) {
        SubOrder subOrder = subOrderRepo
                .findById(subOrderId)
                .orElseThrow(() -> new ResourceNotFoundException(Message.format(Message.Order.SUB_ORDER_NOT_FOUND, subOrderId)));


        stateMachine.assertValidTransition(subOrder.getStatus(), target);

        // ponytail: CAS gate trước inventory — thua race thì throw ngay, không gọi inventory 2 lần
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
                    // Chưa trừ kho → chỉ nhả reservation
                    inventoryClient.release(items, subOrder.getOrderId());
                } else {
                    // Đã trừ kho (từ PAID/PROCESSING) → nhập kho bù
                    inventoryClient.restock(items, staffId);
                }
            }
        } catch (RuntimeException e) {
            // Compensate: nhả claim CAS nếu inventory fail
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
        return toDto(fresh);
    }

    @Override
    @Transactional
    public SubOrderDto cancel(Long subOrderId, Long userId) {
        // ponytail: cancel = transition(CANCELLED), xóa logic duplicate
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


            // ponytail: CAS PENDING_PAYMENT->EXPIRED, đơn đã PAID thì skip (tránh ghi đè)
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
        return orderItemRepo.findBySubOrderId(subOrderId)
                .stream()
                .map(item -> new StockDecrementCommand(item.getSkuId(), item.getQuantity()))
                .toList();
    }

    // Risk-gate docs 13.5: đơn guest bị hủy/hết hạn/thanh toán fail thì cộng lịch sử SĐT
    private void recordGuestAbuse(Long orderId) {
        try {
            orderRepo.findById(orderId).ifPresent(o -> {
                if (o.getUserId() == null && o.getGuestPhone() != null) {
                    guestRiskPort.recordPhoneAbuse(o.getGuestPhone());
                }
            });
        } catch (RuntimeException e) {
            // Ghi lịch sử không được làm hỏng flow chính
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
