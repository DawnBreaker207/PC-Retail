package com.dawn.payment.service;

import com.dawn.common.exception.wrapper.ConflictException;
import com.dawn.common.exception.wrapper.InvalidRequestException;
import com.dawn.payment.dto.CreatePaymentResult;
import com.dawn.payment.entity.Payment;
import com.dawn.payment.entity.enums.PaymentMethod;
import com.dawn.payment.entity.enums.PaymentResultStatus;
import com.dawn.payment.infra.gateway.PaymentGateway;
import com.dawn.payment.infra.gateway.PaymentGatewayRegistry;
import com.dawn.common.contract.OrderClientPort;
import com.dawn.payment.port.out.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreatePaymentServiceTest {

    @Mock
    PaymentRepository paymentRepo;
    @Mock
    OrderClientPort orderClient;
    @Mock
    PaymentGatewayRegistry gatewayRegistry;
    @Mock
    PaymentGateway gateway;

    CreatePaymentService service;

    @BeforeEach
    void setUp() {
        service = new CreatePaymentService(paymentRepo, orderClient, gatewayRegistry);
        lenient().when(gatewayRegistry.get(any())).thenReturn(gateway);
        lenient().when(gateway.createPayment(any())).thenAnswer(inv -> {
            Payment p = inv.getArgument(0);
            return new CreatePaymentResult(p.getId(), "https://pay/" + p.getId());
        });
    }

    @Test
    @DisplayName("reserved_until đã qua: hard-block 409, không tạo payment")
    void expiredOrder_conflict() {
        when(orderClient.getOrderReservedUntil(1L)).thenReturn(Instant.now().minusSeconds(60));

        assertThatThrownBy(() -> service.create(1L, PaymentMethod.VNPAY, 9L))
                .isInstanceOf(ConflictException.class);

        verify(paymentRepo, never()).save(any());
    }

    @Test
    @DisplayName("Đã có PENDING cùng method: tái dùng, không tạo mới")
    void existingPending_reused() {
        when(orderClient.getOrderReservedUntil(1L)).thenReturn(Instant.now().plusSeconds(600));
        Payment pending = Payment.builder().orderId(1L).method(PaymentMethod.VNPAY)
                .amount(BigDecimal.valueOf(100000)).status(PaymentResultStatus.PENDING).build();
        pending.setId(5L);
        when(paymentRepo.findByOrderIdAndMethod(1L, PaymentMethod.VNPAY)).thenReturn(List.of(pending));

        CreatePaymentResult res = service.create(1L, PaymentMethod.VNPAY, 9L);

        assertThat(res.paymentId()).isEqualTo(5L);
        verify(paymentRepo, never()).save(any());
    }

    @Test
    @DisplayName("Tổng tiền <= 0: 400")
    void invalidAmount_badRequest() {
        when(orderClient.getOrderReservedUntil(1L)).thenReturn(Instant.now().plusSeconds(600));
        when(paymentRepo.findByOrderIdAndMethod(1L, PaymentMethod.VNPAY)).thenReturn(List.of());
        when(orderClient.getOrderTotalAmount(1L)).thenReturn(BigDecimal.ZERO);

        assertThatThrownBy(() -> service.create(1L, PaymentMethod.VNPAY, 9L))
                .isInstanceOf(InvalidRequestException.class);
    }

    @Test
    @DisplayName("Thiếu method: mặc định VNPAY, không nổ 500")
    void nullMethod_defaultsVnpay() {
        when(orderClient.getOrderReservedUntil(1L)).thenReturn(Instant.now().plusSeconds(600));
        when(paymentRepo.findByOrderIdAndMethod(1L, PaymentMethod.VNPAY)).thenReturn(List.of());
        when(orderClient.getOrderTotalAmount(1L)).thenReturn(BigDecimal.valueOf(100000));

        CreatePaymentResult res = service.create(1L, null, 9L);

        assertThat(res).isNotNull();
        verify(paymentRepo).save(argThat(p -> p.getMethod() == PaymentMethod.VNPAY));
    }
}
