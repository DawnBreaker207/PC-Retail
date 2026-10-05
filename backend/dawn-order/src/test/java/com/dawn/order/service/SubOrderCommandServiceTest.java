package com.dawn.order.service;

import com.dawn.common.exception.wrapper.ConflictException;
import com.dawn.common.redis.service.RedisService;
import com.dawn.order.entity.SubOrder;
import com.dawn.order.entity.enums.FulfillmentType;
import com.dawn.order.entity.enums.SubOrderStatus;
import com.dawn.order.port.out.InventoryClientPort;
import com.dawn.order.port.out.OrderItemRepository;
import com.dawn.order.port.out.SubOrderRepository;
import com.dawn.order.statemachine.SubOrderStateMachine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
class SubOrderCommandServiceTest {

    @Mock
    SubOrderRepository subOrderRepo;
    @Mock
    OrderItemRepository orderItemRepo;
    @Mock
    InventoryClientPort inventoryClient;
    @Mock
    RedisService redisService;

    SubOrderCommandService service;

    @BeforeEach
    void setUp() {
        service = new SubOrderCommandService(subOrderRepo, orderItemRepo,
                new SubOrderStateMachine(), inventoryClient, redisService);
    }

    private SubOrder subOrder(SubOrderStatus status) {
        return SubOrder.builder()
                .orderId(7L)
                .fulfillmentType(FulfillmentType.DELIVERY)
                .subtotal(BigDecimal.TEN)
                .finalAmount(BigDecimal.TEN)
                .status(status)
                .statusHistory(new ArrayList<>(List.of()))
                .build();
    }

    @Test
    @DisplayName("Hủy từ PENDING_PAYMENT: release reservation, không restock")
    void cancelFromPending_releases() {
        SubOrder so = subOrder(SubOrderStatus.PENDING_PAYMENT);
        when(subOrderRepo.findById(1L)).thenReturn(Optional.of(so));
        when(subOrderRepo.transitionIfStatus(1L, SubOrderStatus.PENDING_PAYMENT, SubOrderStatus.CANCELLED))
                .thenReturn(1);
        when(orderItemRepo.findBySubOrderId(any())).thenReturn(List.of());

        service.transition(1L, SubOrderStatus.CANCELLED, 9L);

        verify(inventoryClient).release(anyList(), eq(7L));
        verify(inventoryClient, never()).restock(anyList(), anyLong());
    }

    @Test
    @DisplayName("Hủy từ PAID (đã trừ kho): restock bù, không release")
    void cancelFromPaid_restocks() {
        SubOrder so = subOrder(SubOrderStatus.PAID);
        when(subOrderRepo.findById(1L)).thenReturn(Optional.of(so));
        when(subOrderRepo.transitionIfStatus(1L, SubOrderStatus.PAID, SubOrderStatus.CANCELLED))
                .thenReturn(1);
        when(orderItemRepo.findBySubOrderId(any())).thenReturn(List.of());

        service.transition(1L, SubOrderStatus.CANCELLED, 9L);

        verify(inventoryClient).restock(anyList(), eq(9L));
        verify(inventoryClient, never()).release(anyList(), anyLong());
    }

    @Test
    @DisplayName("Thua race CAS: throw Conflict, không đụng inventory")
    void lostRace_throwsWithoutInventory() {
        SubOrder so = subOrder(SubOrderStatus.PAID);
        when(subOrderRepo.findById(1L)).thenReturn(Optional.of(so));
        when(subOrderRepo.transitionIfStatus(1L, SubOrderStatus.PAID, SubOrderStatus.PROCESSING))
                .thenReturn(0);

        assertThatThrownBy(() -> service.transition(1L, SubOrderStatus.PROCESSING, 9L))
                .isInstanceOf(ConflictException.class);

        verifyNoInteractions(inventoryClient);
    }
}
