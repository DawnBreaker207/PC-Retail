package com.dawn.order.service;

import com.dawn.common.exception.wrapper.InvalidRequestException;
import com.dawn.order.controller.request.CheckoutRequest;
import com.dawn.order.dto.CartItemSnapshot;
import com.dawn.order.dto.OrderDto;
import com.dawn.order.dto.VoucherApplyResult;
import com.dawn.order.entity.Order;
import com.dawn.order.entity.enums.FulfillmentType;
import com.dawn.order.port.out.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CheckoutServiceTest {
    @Mock
    OrderRepository orderRepository;

    @Mock
    SubOrderRepository subOrderRepository;

    @Mock
    OrderItemRepository orderItemRepository;

    @Mock
    OrderQueryService orderQueryService;

    @Mock
    CartClientPort cartClient;

    @Mock
    InventoryClientPort inventoryClient;

    @Mock
    PromotionClientPort promotionClient;

    @Mock
    CatalogClientPort catalogClient;

    @InjectMocks
    CheckoutService checkoutService;


    // Idempotency

    @Nested
    @DisplayName("Idempotency - duplicate requests must return the existing order")
    class Idempotency {

        @Test
        @DisplayName("Same idempotency key -> return existing order, skip all processing")
        void duplicateKey_returnsExistingOrder() {
            String key = UUID.randomUUID().toString();
            Order existing = new Order();
            OrderDto existingDto = mock(OrderDto.class);

            when(orderRepository.findByIdempotencyKey(key)).thenReturn(Optional.of(existing));
            when(orderQueryService.toOrderDto(existing)).thenReturn(existingDto);

            OrderDto result = checkoutService.checkout(buildRequest(key, FulfillmentType.PICKUP), 1L);

            assertThat(result).isSameAs(existingDto);
            verifyNoInteractions(cartClient);
        }
    }

    // Validation
    @Nested
    @DisplayName("Validation — cart and address checks")
    class Validation {

        @Test
        @DisplayName("Empty cart -> throw InvalidRequestException")
        void emptyCart_throws() {
            when(orderRepository.findByIdempotencyKey(any())).thenReturn(Optional.empty());
            when(cartClient.getItems(1L)).thenReturn(List.of());

            assertThatThrownBy(() ->
                    checkoutService.checkout(buildRequest(UUID.randomUUID().toString(), FulfillmentType.PICKUP), 1L))
                    .isInstanceOf(InvalidRequestException.class);
        }

        @Test
        @DisplayName("DELIVERY fulfillment without shipping address -> throw InvalidRequestException")
        void deliveryWithoutAddress_throws() {
            when(orderRepository.findByIdempotencyKey(any())).thenReturn(Optional.empty());
            when(cartClient.getItems(1L)).thenReturn(List.of(new CartItemSnapshot(10L, 2)));

            CheckoutRequest req = new CheckoutRequest(
                    UUID.randomUUID().toString(),
                    FulfillmentType.DELIVERY,
                    null,
                    null,
                    null
            );

            assertThatThrownBy(() -> checkoutService.checkout(req, 1L))
                    .isInstanceOf(InvalidRequestException.class);
        }

        @Test
        @DisplayName("PICKUP fulfillment without address -> passes address validation")
        void pickupWithoutAddress_passesValidation() {
            when(orderRepository.findByIdempotencyKey(any())).thenReturn(Optional.empty());
            when(cartClient.getItems(1L)).thenReturn(List.of(new CartItemSnapshot(10L, 2)));
            when(catalogClient.getPrices(anyList())).thenReturn(Map.of(10L, BigDecimal.valueOf(500_000)));
            when(inventoryClient.decrementBatch(anyList())).thenReturn(List.of());

            // May fail at a later step — what matters is no InvalidRequestException for address
            assertThatThrownBy(() ->
                    checkoutService.checkout(buildRequest(UUID.randomUUID().toString(), FulfillmentType.PICKUP), 1L))
                    .isNotInstanceOf(InvalidRequestException.class);
        }
    }


    // Redis compensation
    @Nested
    @DisplayName("Redis compensation - must roll back Redis when DB fails")
    class CompensateRedis {

        @Test
        @DisplayName("Lua gatekeeper passes but DB fails -> must call incrementBatch to restore Redis")
        void dbFails_compensatesRedis() {
            String key = UUID.randomUUID().toString();
            List<CartItemSnapshot> items = List.of(new CartItemSnapshot(10L, 2));

            when(orderRepository.findByIdempotencyKey(key)).thenReturn(Optional.empty());
            when(cartClient.getItems(1L)).thenReturn(items);
            when(catalogClient.getPrices(anyList())).thenReturn(Map.of(10L, BigDecimal.valueOf(500_000)));
            when(inventoryClient.decrementBatch(anyList())).thenReturn(List.of());
            when(orderRepository.save(any())).thenThrow(new RuntimeException("DB down"));

            assertThatThrownBy(() ->
                    checkoutService.checkout(buildRequest(key, FulfillmentType.PICKUP), 1L))
                    .isInstanceOf(RuntimeException.class);

            verify(inventoryClient).incrementBatch(anyList());
        }

        @Test
        @DisplayName("Lua passes + voucher reserved + DB fails -> must roll back both inventory and voucher")
        void dbFails_withVoucher_compensatesBoth() {
            String key = UUID.randomUUID().toString();
            List<CartItemSnapshot> items = List.of(new CartItemSnapshot(10L, 1));

            when(orderRepository.findByIdempotencyKey(key)).thenReturn(Optional.empty());
            when(cartClient.getItems(1L)).thenReturn(items);
            when(catalogClient.getPrices(anyList())).thenReturn(Map.of(10L, BigDecimal.valueOf(500_000)));
            when(promotionClient.validate(anyString(), any()))
                    .thenReturn(VoucherApplyResult.ok(BigDecimal.valueOf(50_000)));
            when(inventoryClient.decrementBatch(anyList())).thenReturn(List.of());
            when(orderRepository.save(any())).thenThrow(new RuntimeException("DB down"));

            CheckoutRequest req = new CheckoutRequest(
                    key,
                    FulfillmentType.PICKUP,
                    null,
                    "SALE10",
                    null);

            assertThatThrownBy(() -> checkoutService.checkout(req, 1L))
                    .isInstanceOf(RuntimeException.class);

            verify(inventoryClient).incrementBatch(anyList());
            verify(promotionClient).validateStack(anyList(), anyList());
            verify(promotionClient).rollBackPromotions(anyList(), anyList());
        }

        @Test
        @DisplayName("Lua gatekeeper fails -> throw immediately, DB never touched, no compensation needed")
        void luaFails_throwsImmediately_noDbCall() {
            String key = UUID.randomUUID().toString();
            List<CartItemSnapshot> items = List.of(new CartItemSnapshot(10L, 2));

            when(orderRepository.findByIdempotencyKey(key)).thenReturn(Optional.empty());
            when(cartClient.getItems(1L)).thenReturn(items);
            when(catalogClient.getPrices(anyList())).thenReturn(Map.of(10L, BigDecimal.valueOf(500_000)));
            when(inventoryClient.decrementBatch(anyList())).thenReturn(List.of(10L)); // SKU 10 failed

            assertThatThrownBy(() ->
                    checkoutService.checkout(buildRequest(key, FulfillmentType.PICKUP), 1L))
                    .isInstanceOf(InvalidRequestException.class);

            verify(orderRepository, never()).save(any());
            verify(inventoryClient, never()).incrementBatch(anyList());
        }
    }

    // Helpers
    private CheckoutRequest buildRequest(String idempotencyKey, FulfillmentType fulfillmentType) {
        return new CheckoutRequest(
                idempotencyKey,
                fulfillmentType,
                fulfillmentType == FulfillmentType.DELIVERY ? Map.of("line1", "123 ABC") : null,
                null,
                null
        );
    }
}
