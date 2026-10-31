package com.dawn.order.service;

import com.dawn.common.exception.wrapper.ConflictException;
import com.dawn.common.exception.wrapper.InvalidRequestException;
import com.dawn.common.redis.service.RedisService;
import com.dawn.order.controller.request.GuestCheckoutRequest;
import com.dawn.common.contract.CartItemSnapshot;
import com.dawn.order.entity.enums.FulfillmentType;
import com.dawn.common.contract.CartClientPort;
import com.dawn.common.contract.CatalogClientPort;
import com.dawn.common.contract.GuestRiskPort;
import com.dawn.common.contract.InventoryClientPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GuestCheckoutServiceTest {

    @Mock
    OrderCommandService orderCommandService;
    @Mock
    OrderQueryService orderQueryService;
    @Mock
    CartClientPort cartClient;
    @Mock
    InventoryClientPort inventoryClient;
    @Mock
    CatalogClientPort catalogClient;
    @Mock
    RedisService redisService;
    @Mock
    GuestRiskPort guestRiskPort;
    @Mock
    ApplicationEventPublisher eventPublisher;

    @InjectMocks
    GuestCheckoutService service;

    @BeforeEach
    void setUp() {
        // @Value fields are not injected outside Spring context; set manually
        org.springframework.test.util.ReflectionTestUtils.setField(service, "guestMaxOrderTotal", 30_000_000L);
        org.springframework.test.util.ReflectionTestUtils.setField(service, "phoneAbuseThreshold", 3L);
    }

    private GuestCheckoutRequest req(String token) {
        return new GuestCheckoutRequest(UUID.randomUUID().toString(), FulfillmentType.PICKUP,
                Map.of(), token, "g@g.vn", "0901");
    }

    @Test
    @DisplayName("Quá 5 checkout/giờ: 409, không chạm cart")
    void rateLimited_conflict() {
        when(redisService.increment(anyString(), eq(1L))).thenReturn(6L);

        assertThatThrownBy(() -> service.checkout(req("tok")))
                .isInstanceOf(ConflictException.class);

        verifyNoInteractions(cartClient);
    }

    @Test
    @DisplayName("Giỏ guest rỗng: 400")
    void emptyCart_badRequest() {
        when(redisService.increment(anyString(), eq(1L))).thenReturn(1L);
        when(guestRiskPort.isPhoneVerified("0901")).thenReturn(true);
        when(guestRiskPort.phoneAbuseCount("0901")).thenReturn(0L);
        when(orderQueryService.findByIdempotencyKey(anyString())).thenReturn(Optional.empty());
        when(cartClient.getGuestItems("tok")).thenReturn(List.of());

        assertThatThrownBy(() -> service.checkout(req("tok")))
                .isInstanceOf(InvalidRequestException.class);

        verify(inventoryClient, never()).decrementBatch(anyList());
    }

    @Test
    @DisplayName("Idempotency trùng: trả đơn cũ, không trừ kho lần 2")
    void duplicateKey_returnsExisting() {
        var existing = mock(com.dawn.order.dto.OrderDto.class);
        when(redisService.increment(anyString(), eq(1L))).thenReturn(1L);
        when(guestRiskPort.isPhoneVerified("0901")).thenReturn(true);
        when(guestRiskPort.phoneAbuseCount("0901")).thenReturn(0L);
        when(orderQueryService.findByIdempotencyKey(anyString())).thenReturn(Optional.of(existing));

        assertThat(service.checkout(req("tok"))).isSameAs(existing);

        verify(inventoryClient, never()).decrementBatch(anyList());
    }

    @Test
    @DisplayName("Happy path guest: đơn user null + clear giỏ guest + đốt OTP")
    void happyPath_createsGuestOrder() {
        when(redisService.increment(anyString(), eq(1L))).thenReturn(1L);
        when(guestRiskPort.isPhoneVerified("0901")).thenReturn(true);
        when(guestRiskPort.phoneAbuseCount("0901")).thenReturn(0L);
        when(orderQueryService.findByIdempotencyKey(anyString())).thenReturn(Optional.empty());
        when(cartClient.getGuestItems("tok")).thenReturn(List.of(new CartItemSnapshot(10L, 2)));
        when(catalogClient.getPrices(List.of(10L))).thenReturn(Map.of(10L, BigDecimal.valueOf(500_000)));
        when(inventoryClient.decrementBatch(anyList())).thenReturn(List.of());
        var dto = mock(com.dawn.order.dto.OrderDto.class);
        when(orderCommandService.createOrder(any(), isNull(), eq("g@g.vn"), eq("0901"),
                anyList(), anyList(), anyMap(), any(), any(), any(), any())).thenReturn(dto);

        assertThat(service.checkout(req("tok"))).isSameAs(dto);

        verify(cartClient).clearGuestCart("tok");
        verify(guestRiskPort).consumePhoneVerification("0901");
    }

    @Test
    @DisplayName("SĐT chưa verify OTP: 400, không chạm kho")
    void unverifiedPhone_badRequest() {
        when(redisService.increment(anyString(), eq(1L))).thenReturn(1L);
        when(guestRiskPort.isPhoneVerified("0901")).thenReturn(false);

        assertThatThrownBy(() -> service.checkout(req("tok")))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("verify");

        verify(inventoryClient, never()).decrementBatch(anyList());
    }

    @Test
    @DisplayName("SĐT bùng 3 lần: chặn, ép login + online")
    void abusedPhone_blocked() {
        when(redisService.increment(anyString(), eq(1L))).thenReturn(1L);
        when(guestRiskPort.isPhoneVerified("0901")).thenReturn(true);
        when(guestRiskPort.phoneAbuseCount("0901")).thenReturn(3L);

        assertThatThrownBy(() -> service.checkout(req("tok")))
                .isInstanceOf(InvalidRequestException.class);

        verify(inventoryClient, never()).decrementBatch(anyList());
    }

    @Test
    @DisplayName("Vượt ngưỡng 30M: chặn, không trừ kho")
    void overThreshold_blocked() {
        when(redisService.increment(anyString(), eq(1L))).thenReturn(1L);
        when(guestRiskPort.isPhoneVerified("0901")).thenReturn(true);
        when(guestRiskPort.phoneAbuseCount("0901")).thenReturn(0L);
        when(orderQueryService.findByIdempotencyKey(anyString())).thenReturn(Optional.empty());
        when(cartClient.getGuestItems("tok")).thenReturn(List.of(new CartItemSnapshot(10L, 2)));
        when(catalogClient.getPrices(List.of(10L))).thenReturn(Map.of(10L, BigDecimal.valueOf(20_000_000)));

        assertThatThrownBy(() -> service.checkout(req("tok")))
                .isInstanceOf(InvalidRequestException.class);

        verify(inventoryClient, never()).decrementBatch(anyList());
    }
}
