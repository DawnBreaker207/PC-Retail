package com.dawn.order.service;

import com.dawn.common.exception.wrapper.ResourceNotFoundException;
import com.dawn.common.message.Message;
import com.dawn.order.dto.StockDecrementCommand;
import com.dawn.order.dto.SubOrderDto;
import com.dawn.order.entity.SubOrder;
import com.dawn.order.entity.enums.SubOrderStatus;
import com.dawn.order.port.in.TransitionSubOrderUseCase;
import com.dawn.order.port.out.InventoryClientPort;
import com.dawn.order.port.out.OrderItemRepositoryPort;
import com.dawn.order.port.out.SubOrderRepositoryPort;
import com.dawn.order.statemachine.SubOrderStateMachine;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SubOrderCommandService implements TransitionSubOrderUseCase {

    private final SubOrderRepositoryPort subOrderRepo;
    private final OrderItemRepositoryPort orderItemRepo;
    private final SubOrderStateMachine stateMachine;
    private final InventoryClientPort inventoryClient;

    @Override
    @Transactional
    public SubOrderDto transition(Long subOrderId, SubOrderStatus target, Long staffId) {
        SubOrder subOrder = subOrderRepo
                .findById(subOrderId)
                .orElseThrow(() -> new ResourceNotFoundException(Message.Order.SUB_ORDER_NOT_FOUND));


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
    public SubOrderDto cancel(Long subOrderId, Long userId) {
        SubOrder subOrder = subOrderRepo
                .findById(subOrderId)
                .orElseThrow(() -> new ResourceNotFoundException(Message.Order.SUB_ORDER_NOT_FOUND));
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
    public void expiredUnpaidOrders() {
        Instant threshold = Instant.now().minusSeconds(15 * 60);
        List<SubOrder> expired = subOrderRepo.findByStatusAndCreatedAtBefore(SubOrderStatus.PENDING_PAYMENT, threshold);

        for (SubOrder subOrder : expired) {
            List<StockDecrementCommand> items = toStockCommands(subOrder.getId());
            inventoryClient.release(items, subOrder.getOrderId());

            appendHistory(subOrder, SubOrderStatus.EXPIRED, null);
            subOrder.setStatus(SubOrderStatus.EXPIRED);
            subOrderRepo.save(subOrder);
        }
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
