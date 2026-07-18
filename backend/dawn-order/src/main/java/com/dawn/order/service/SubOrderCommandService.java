package com.dawn.order.service;

import com.dawn.common.auditlog.LogConstant;
import com.dawn.common.auditlog.Loggable;
import com.dawn.common.exception.wrapper.ResourceNotFoundException;
import com.dawn.common.message.Message;
import com.dawn.order.dto.StockDecrementCommand;
import com.dawn.order.dto.SubOrderDto;
import com.dawn.order.entity.OrderItem;
import com.dawn.order.entity.SubOrder;
import com.dawn.order.entity.enums.SubOrderStatus;
import com.dawn.order.port.in.TransitionSubOrderUseCase;
import com.dawn.order.port.out.InventoryClientPort;
import com.dawn.order.port.out.OrderItemRepository;
import com.dawn.order.port.out.SubOrderRepository;
import com.dawn.order.statemachine.SubOrderStateMachine;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubOrderCommandService implements TransitionSubOrderUseCase {

    private final SubOrderRepository subOrderRepo;
    private final OrderItemRepository orderItemRepo;
    private final SubOrderStateMachine stateMachine;
    private final InventoryClientPort inventoryClient;

    @Override
    @Transactional
    @Loggable(action = LogConstant.Action.TRANSITION_ORDER, entity = LogConstant.Entity.SUB_ORDER)
    public SubOrderDto transition(Long subOrderId, SubOrderStatus target, Long staffId) {
        SubOrder subOrder = subOrderRepo
                .findById(subOrderId)
                .orElseThrow(() -> new ResourceNotFoundException(Message.format(Message.Order.SUB_ORDER_NOT_FOUND, subOrderId)));


        stateMachine.assertValidTransition(subOrder.getStatus(), target);

        if (target == SubOrderStatus.PAID) {
            List<StockDecrementCommand> items = toStockCommands(subOrder.getId());
            inventoryClient.deduct(items, subOrder.getOrderId());
        }
        if (target == SubOrderStatus.CANCELLED || target == SubOrderStatus.EXPIRED) {
            List<StockDecrementCommand> items = toStockCommands(subOrder.getId());
            inventoryClient.release(items, subOrder.getOrderId());
        }

        appendHistory(subOrder, target, staffId);
        subOrder.setStatus(target);
        subOrderRepo.save(subOrder);
        return toDto(subOrder);
    }

    @Override
    @Transactional
    public SubOrderDto cancel(Long subOrderId, Long userId) {
        SubOrder subOrder = subOrderRepo
                .findById(subOrderId)
                .orElseThrow(() -> new ResourceNotFoundException(Message.format(Message.Order.SUB_ORDER_NOT_FOUND, subOrderId)));
        // User only can self cancel when in PENDING_PAYMENT
        stateMachine.assertValidTransition(subOrder.getStatus(), SubOrderStatus.CANCELLED);

        List<StockDecrementCommand> items = toStockCommands(subOrder.getId());
        inventoryClient.release(items, subOrder.getOrderId());

        appendHistory(subOrder, SubOrderStatus.CANCELLED, userId);
        subOrder.setStatus(SubOrderStatus.CANCELLED);
        subOrderRepo.save(subOrder);
        return toDto(subOrder);
    }

    @Override
    @Transactional
    public void expiredUnpaidOrders() {
        Instant threshold = Instant.now().minusSeconds(120 * 60);
        List<SubOrder> expired = subOrderRepo.findByStatusAndReservedUntilBefore(SubOrderStatus.PENDING_PAYMENT, threshold);

        List<Long> subOrderIds = expired.stream().map(SubOrder::getId).toList();
        Map<Long, List<StockDecrementCommand>> itemsMap = orderItemRepo.findBySubOrderIdIn(subOrderIds)
                .stream()
                .collect(Collectors.groupingBy(
                        OrderItem::getSubOrderId,
                        Collectors.mapping(
                                item -> new StockDecrementCommand(item.getSkuId(), item.getQuantity()),
                                Collectors.toList())));

        for (SubOrder subOrder : expired) {
            List<StockDecrementCommand> items = itemsMap.getOrDefault(subOrder.getId(), List.of());
            inventoryClient.release(items, subOrder.getOrderId());

            appendHistory(subOrder, SubOrderStatus.EXPIRED, null);
            subOrder.setStatus(SubOrderStatus.EXPIRED);
        }
        subOrderRepo.saveAll(expired);
    }

    private List<StockDecrementCommand> toStockCommands(Long subOrderId) {
        return orderItemRepo.findBySubOrderId(subOrderId)
                .stream()
                .map(item -> new StockDecrementCommand(item.getSkuId(), item.getQuantity()))
                .toList();
    }

    private void appendHistory(SubOrder subOrder, SubOrderStatus to, Long actorId) {
        subOrder.getStatusHistory().add(Map.of(
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
