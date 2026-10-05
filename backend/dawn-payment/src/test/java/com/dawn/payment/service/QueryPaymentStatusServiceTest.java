package com.dawn.payment.service;

import com.dawn.common.exception.wrapper.InvalidRequestException;
import com.dawn.payment.dto.PaymentDto;
import com.dawn.payment.entity.Payment;
import com.dawn.payment.entity.enums.PaymentMethod;
import com.dawn.payment.entity.enums.PaymentResultStatus;
import com.dawn.payment.infra.gateway.PaymentGatewayRegistry;
import com.dawn.payment.infra.vnpay.VnpayGatewayAdapter;
import com.dawn.payment.port.out.OrderClientPort;
import com.dawn.payment.port.out.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QueryPaymentStatusServiceTest {

    @Mock
    PaymentRepository paymentRepo;
    @Mock
    PaymentGatewayRegistry gatewayRegistry;
    @Mock
    OrderClientPort orderClient;
    @Mock
    VnpayGatewayAdapter vnpay;

    QueryPaymentStatusService service;

    @BeforeEach
    void setUp() {
        service = new QueryPaymentStatusService(paymentRepo, gatewayRegistry, orderClient);
    }

    private Payment payment(PaymentResultStatus status) {
        return payment(PaymentMethod.VNPAY, status);
    }

    private Payment payment(PaymentMethod method, PaymentResultStatus status) {
        return Payment.builder()
                .orderId(7L)
                .method(method)
                .amount(BigDecimal.valueOf(100000))
                .status(status)
                .build();
    }

    private Map<String, String> webhookPayload() {
        Map<String, String> p = new HashMap<>();
        p.put("vnp_TxnRef", "3");
        p.put("vnp_ResponseCode", "00");
        p.put("vnp_TransactionNo", "txn-9");
        p.put("vnp_SecureHash", "abc");
        return p;
    }

    @Test
    @DisplayName("Webhook duplicate (đã COMPLETED): trả về, không gọi markOrderPaid lần 2")
    void duplicateWebhook_idempotent() {
        Payment done = payment(PaymentResultStatus.COMPLETED);
        when(paymentRepo.findById(3L)).thenReturn(Optional.of(done));
        when(gatewayRegistry.get(PaymentMethod.VNPAY)).thenReturn(vnpay);
        when(vnpay.verifyCallback(anyMap())).thenReturn(true);

        PaymentDto dto = service.handleWebhook("vnpay", webhookPayload());

        assertThat(dto.status()).isEqualTo(PaymentResultStatus.COMPLETED);
        verify(orderClient, never()).markOrderPaid(anyLong());
        verify(paymentRepo, never()).save(any());
    }

    @Test
    @DisplayName("Webhook lần đầu thành công: COMPLETED + markOrderPaid 1 lần")
    void firstWebhook_marksPaidOnce() {
        Payment pending = payment(PaymentResultStatus.PENDING);
        when(paymentRepo.findById(3L)).thenReturn(Optional.of(pending));
        when(gatewayRegistry.get(PaymentMethod.VNPAY)).thenReturn(vnpay);
        when(vnpay.verifyCallback(anyMap())).thenReturn(true);

        PaymentDto dto = service.handleWebhook("vnpay", webhookPayload());

        assertThat(dto.status()).isEqualTo(PaymentResultStatus.COMPLETED);
        verify(orderClient, times(1)).markOrderPaid(7L);
    }

    @Test
    @DisplayName("Callback provider lạ: 400 InvalidRequest thay vì 500")
    void unknownProvider_badRequest() {
        assertThatThrownBy(() -> service.handleCallback("stripe", Map.of()))
                .isInstanceOf(InvalidRequestException.class);
    }

    @Test
    @DisplayName("Mock TIMEOUT qua re-check: UNKNOWN để cron retry, không FAILED oan")
    void mockTimeout_unknown() {
        com.dawn.payment.infra.mock.MockGatewayAdapter mock =
                new com.dawn.payment.infra.mock.MockGatewayAdapter();
        mock.programScenario(21L, "TIMEOUT");
        when(gatewayRegistry.get(PaymentMethod.MOCK)).thenReturn(mock);
        Payment pending21 = payment(PaymentMethod.MOCK, PaymentResultStatus.PENDING);
        pending21.setId(21L);
        Payment pending = pending21;
        when(paymentRepo.findById(21L)).thenReturn(Optional.of(pending));

        PaymentDto dto = service.queryAndSync(21L);

        assertThat(dto.status()).isEqualTo(PaymentResultStatus.UNKNOWN);
        verify(orderClient, never()).markOrderPaymentFailed(anyLong());
    }

    @Test
    @DisplayName("Mock PENDING qua re-check: giữ PENDING, không FAILED")
    void mockPending_stays() {
        com.dawn.payment.infra.mock.MockGatewayAdapter mock =
                new com.dawn.payment.infra.mock.MockGatewayAdapter();
        mock.programScenario(22L, "PENDING");
        when(gatewayRegistry.get(PaymentMethod.MOCK)).thenReturn(mock);
        Payment pending22 = payment(PaymentMethod.MOCK, PaymentResultStatus.PENDING);
        pending22.setId(22L);
        Payment pending = pending22;
        when(paymentRepo.findById(22L)).thenReturn(Optional.of(pending));

        PaymentDto dto = service.queryAndSync(22L);

        assertThat(dto.status()).isEqualTo(PaymentResultStatus.PENDING);
        verify(orderClient, never()).markOrderPaymentFailed(anyLong());
        verify(orderClient, never()).markOrderPaid(anyLong());
    }
}
