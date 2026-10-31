package com.dawn.order.infra.bridge;

import com.dawn.common.exception.wrapper.ConflictException;
import com.dawn.common.exception.wrapper.ResourceNotFoundException;
import com.dawn.common.message.Message;
import com.dawn.common.contract.StockDecrementCommand;
import com.dawn.order.entity.Order;
import com.dawn.order.entity.OrderItem;
import com.dawn.order.entity.SubOrder;
import com.dawn.order.entity.enums.PaymentStatus;
import com.dawn.order.entity.enums.SubOrderStatus;
import com.dawn.common.contract.InventoryClientPort;
import com.dawn.common.contract.GuestRiskPort;
import com.dawn.order.port.out.OrderItemRepository;
import com.dawn.order.port.out.OrderRepository;
import com.dawn.order.port.out.SubOrderRepository;
import com.dawn.order.statemachine.SubOrderStateMachine;
import com.dawn.common.contract.OrderClientPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OrderClientAdapter implements OrderClientPort {

    private final OrderRepository orderRepository;
    private final SubOrderRepository subOrderRepository;
    private final SubOrderStateMachine stateMachine;
    private final InventoryClientPort inventoryClient;
    private final OrderItemRepository orderItemRepository;
    private final GuestRiskPort guestRiskPort;

    @Override
    public BigDecimal getOrderTotalAmount(Long orderId) {
        return orderRepository.findById(orderId)
                .map(Order::getTotalAmount)
                .orElse(null);
    }

    @Override
    public Instant getOrderReservedUntil(Long orderId) {
        return orderRepository.findById(orderId)
                .map(Order::getReservedUntil)
                .orElse(null);
    }
    @Transactional
    @Override
    public void markOrderPaid(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(Message.format(Message.Order.NOT_FOUND, orderId)));
        order.setPaymentStatus(PaymentStatus.PAID);
        orderRepository.save(order);

        List<SubOrder> subOrders = subOrderRepository.findByOrderId(orderId);


        for (SubOrder so : subOrders) {
            stateMachine.assertValidTransition(so.getStatus(), SubOrderStatus.PAID);
            String fromStatus = so.getStatus().name();
            // CAS PENDING_PAYMENT→PAID; concurrent expiry loses the race, preventing EXPIRED revival
            int claimed = subOrderRepository.transitionIfStatus(so.getId(), SubOrderStatus.PENDING_PAYMENT, SubOrderStatus.PAID);
            if (claimed == 0) {
                throw new ConflictException(Message.format(Message.Order.INVALID_TRANSITION, fromStatus, SubOrderStatus.PAID));
            }
            SubOrder fresh = subOrderRepository.findById(so.getId()).orElse(so);
            fresh.getStatusHistory().add(Map.of(
                    "from", fromStatus,
                    "to", SubOrderStatus.PAID.name(),
                    "createdAt", Instant.now().toString()));
            subOrderRepository.save(fresh);
        }
        List<OrderItem> items = subOrders.stream()
                .flatMap(so -> orderItemRepository.findBySubOrderId(so.getId()).stream())
                .toList();
        List<StockDecrementCommand> deductItems = items.stream()
                .map(i -> new StockDecrementCommand(i.getSkuId(), i.getQuantity()))
                .toList();
        inventoryClient.deduct(deductItems, orderId);
    }

    @Transactional
    @Override
    public void markOrderPaymentFailed(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(Message.format(Message.Order.NOT_FOUND, orderId)));
        order.setPaymentStatus(PaymentStatus.FAILED);
        orderRepository.save(order);
        // track phone abuse for failed guest orders
        if (order.getUserId() == null && order.getGuestPhone() != null) {
            guestRiskPort.recordPhoneAbuse(order.getGuestPhone());
        }


        List<SubOrder> subOrders = subOrderRepository.findByOrderId(orderId);


        for (SubOrder so : subOrders) {
            stateMachine.assertValidTransition(so.getStatus(), SubOrderStatus.CANCELLED);
            String fromStatus = so.getStatus().name();
            // CAS guard: orders already past PENDING_PAYMENT cannot be pulled back to CANCELLED
            int claimed = subOrderRepository.transitionIfStatus(so.getId(), SubOrderStatus.PENDING_PAYMENT, SubOrderStatus.CANCELLED);
            if (claimed == 0) {
                throw new ConflictException(Message.format(Message.Order.INVALID_TRANSITION, fromStatus, SubOrderStatus.CANCELLED));
            }
            SubOrder fresh = subOrderRepository.findById(so.getId()).orElse(so);
            fresh.getStatusHistory().add(Map.of(
                    "from", fromStatus,
                    "to", SubOrderStatus.CANCELLED.name(),
                    "actorId", "SYSTEM",
                    "createdAt", Instant.now().toString()));
            subOrderRepository.save(fresh);
        }
        List<OrderItem> items = subOrders
                .stream()
                .flatMap(so -> orderItemRepository.findBySubOrderId(so.getId()).stream())
                .toList();

        List<StockDecrementCommand> releaseItems = items.stream()
                .map(i -> new StockDecrementCommand(i.getSkuId(), i.getQuantity()))
                .toList();
        inventoryClient.release(releaseItems, orderId);
    }
}
