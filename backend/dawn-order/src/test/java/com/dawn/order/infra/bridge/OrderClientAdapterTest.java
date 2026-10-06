package com.dawn.order.infra.bridge;

import com.dawn.common.exception.wrapper.ConflictException;
import com.dawn.common.exception.wrapper.ResourceNotFoundException;
import com.dawn.order.entity.Order;
import com.dawn.order.entity.SubOrder;
import com.dawn.order.entity.enums.FulfillmentType;
import com.dawn.order.entity.enums.PaymentStatus;
import com.dawn.order.entity.enums.SubOrderStatus;
import com.dawn.order.port.out.GuestRiskPort;
import com.dawn.order.port.out.InventoryClientPort;
import com.dawn.order.port.out.OrderItemRepository;
import com.dawn.order.port.out.OrderRepository;
import com.dawn.order.port.out.SubOrderRepository;
import com.dawn.order.statemachine.SubOrderStateMachine;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderClientAdapterTest {

    @Mock
    OrderRepository orderRepository;
    @Mock
    SubOrderRepository subOrderRepository;
    @Mock
    InventoryClientPort inventoryClient;
    @Mock
    OrderItemRepository orderItemRepository;
    @Mock
    GuestRiskPort guestRiskPort;

    SubOrderStateMachine stateMachine = new SubOrderStateMachine();

    OrderClientAdapter adapter() {
        return new OrderClientAdapter(orderRepository, subOrderRepository,
                stateMachine, inventoryClient, orderItemRepository, guestRiskPort);
    }

    private Order order() {
        Order o = Order.builder().userId(2L).totalAmount(BigDecimal.TEN)
                .paymentStatus(PaymentStatus.PENDING).idempotencyKey("k").build();
        o.setId(1L);
        return o;
    }

    private SubOrder subOrder(SubOrderStatus status) {
        SubOrder so = SubOrder.builder().orderId(1L).fulfillmentType(FulfillmentType.DELIVERY)
                .subtotal(BigDecimal.TEN).finalAmount(BigDecimal.TEN)
                .status(status).statusHistory(new ArrayList<>(List.of())).build();
        so.setId(11L);
        return so;
    }

    @Test
    @DisplayName("markOrderPaid đơn không tồn tại: 404")
    void paid_missingOrder_404() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adapter().markOrderPaid(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("markOrderPaid: CAS thắng thì deduct kho")
    void paid_casWin_deducts() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order()));
        SubOrder so = subOrder(SubOrderStatus.PENDING_PAYMENT);
        when(subOrderRepository.findByOrderId(1L)).thenReturn(List.of(so));
        when(subOrderRepository.transitionIfStatus(11L, SubOrderStatus.PENDING_PAYMENT, SubOrderStatus.PAID))
                .thenReturn(1);
        when(subOrderRepository.findById(11L)).thenReturn(Optional.of(so));

        adapter().markOrderPaid(1L);

        verify(inventoryClient).deduct(anyList(), eq(1L));
    }

    @Test
    @DisplayName("markOrderPaid khi đơn đã EXPIRED: Conflict, không deduct, không hồi sinh")
    void paid_afterExpired_conflict() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order()));
        SubOrder so = subOrder(SubOrderStatus.EXPIRED);
        when(subOrderRepository.findByOrderId(1L)).thenReturn(List.of(so));

        assertThatThrownBy(() -> adapter().markOrderPaid(1L))
                .isInstanceOf(ConflictException.class);

        verify(inventoryClient, never()).deduct(anyList(), anyLong());
    }
}
