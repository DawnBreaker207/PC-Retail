package com.dawn.order.infra.bridge;

import com.dawn.order.entity.Order;
import com.dawn.order.entity.SubOrder;
import com.dawn.order.entity.enums.PaymentStatus;
import com.dawn.order.entity.enums.SubOrderStatus;
import com.dawn.order.port.out.OrderRepository;
import com.dawn.order.port.out.SubOrderRepository;
import com.dawn.order.statemachine.SubOrderStateMachine;
import com.dawn.payment.port.out.OrderClientPort;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OrderClientAdapter implements OrderClientPort {

    private final OrderRepository orderRepo;
    private final SubOrderRepository subOrderRepo;
    private final SubOrderStateMachine stateMachine;

    @Override
    public BigDecimal getOrderTotalAmount(Long orderId) {
        return orderRepo.findById(orderId)
                .map(Order::getTotalAmount)
                .orElse(null);
    }

    @Override
    public Instant getOrderReservedUntil(Long orderId) {
        return orderRepo.findById(orderId)
                .map(Order::getReservedUntil)
                .orElse(null);
    }

    @Transactional
    @Override
    public void markOrderPaid(Long orderId) {
        Order order = orderRepo.findById(orderId).orElseThrow();
        order.setPaymentStatus(PaymentStatus.PAID);
        orderRepo.save(order);

        List<SubOrder> subOrders = subOrderRepo.findByOrderId(orderId);
        for (SubOrder so : subOrders) {
            stateMachine.assertValidTransition(so.getStatus(), SubOrderStatus.PAID);
            so.setStatus(SubOrderStatus.PAID);
            subOrderRepo.save(so);
        }
    }

    @Transactional
    @Override
    public void markOrderPaymentFailed(Long orderId) {
        Order order = orderRepo.findById(orderId).orElseThrow();
        order.setPaymentStatus(PaymentStatus.FAILED);
        orderRepo.save(order);
    }
}
