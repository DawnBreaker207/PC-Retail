package com.dawn.order.service;

import com.dawn.common.event.OrderCreatedEvent;
import com.dawn.common.exception.wrapper.ConflictException;
import com.dawn.common.exception.wrapper.InvalidRequestException;
import com.dawn.common.message.Message;
import com.dawn.common.redis.service.RedisService;
import com.dawn.order.controller.request.GuestCheckoutRequest;
import com.dawn.order.dto.CartItemSnapshot;
import com.dawn.order.dto.OrderDto;
import com.dawn.order.dto.StockDecrementCommand;
import com.dawn.order.entity.enums.FulfillmentType;
import com.dawn.order.port.out.CartClientPort;
import com.dawn.order.port.out.CatalogClientPort;
import com.dawn.order.port.out.InventoryClientPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Guest checkout (docs 13.5 lite): không login, giỏ theo guest_token,
 * đơn user_id null + liên hệ khách. Không voucher (cần user để chống dùng lại).
 * Risk-gate lite: tối đa 5 checkout/giờ mỗi guest_token.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GuestCheckoutService {

    private static final int MAX_GUEST_CHECKOUT_PER_HOUR = 5;

    private final OrderCommandService orderCommandService;
    private final OrderQueryService orderQueryService;
    private final CartClientPort cartClient;
    private final InventoryClientPort inventoryClient;
    private final CatalogClientPort catalogClient;
    private final RedisService redisService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public OrderDto checkout(GuestCheckoutRequest req) {
        // Risk-gate: rate limit theo guest_token
        String rateKey = "guest:checkout:" + req.guestToken();
        Long count = redisService.increment(rateKey, 1L);
        if (count != null && count == 1L) {
            redisService.expire(rateKey, Duration.ofHours(1));
        }
        if (count != null && count > MAX_GUEST_CHECKOUT_PER_HOUR) {
            throw new ConflictException("Too many guest checkouts, please try again later");
        }

        var existing = orderQueryService.findByIdempotencyKey(req.idempotencyKey());
        if (existing.isPresent()) return existing.get();

        List<CartItemSnapshot> cartItems = cartClient.getGuestItems(req.guestToken());
        if (cartItems.isEmpty()) {
            throw new InvalidRequestException(Message.Order.CART_EMPTY);
        }

        if (req.fulfillmentType() == FulfillmentType.DELIVERY
                && (req.shippingAddress() == null || req.shippingAddress().isEmpty())) {
            throw new InvalidRequestException(Message.Order.SHIPPING_ADDRESS_REQUIRED);
        }

        List<Long> skuIds = cartItems.stream().map(CartItemSnapshot::skuId).toList();
        Map<Long, BigDecimal> priceMap = catalogClient.getPrices(skuIds);
        BigDecimal subTotal = cartItems.stream()
                .map(item -> priceMap.getOrDefault(item.skuId(), BigDecimal.ZERO)
                        .multiply(BigDecimal.valueOf(item.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<StockDecrementCommand> stockItems = cartItems.stream()
                .map(item -> new StockDecrementCommand(item.skuId(), item.quantity()))
                .toList();

        List<Long> failedSkuIds = inventoryClient.decrementBatch(stockItems);
        if (!failedSkuIds.isEmpty()) {
            throw new InvalidRequestException(Message.format(Message.Order.INSUFFICIENT_STOCK_SKUS, failedSkuIds));
        }

        // BE tự tính ship (ghi đè FE) — PICKUP = 0
        BigDecimal shippingFee = req.fulfillmentType() == FulfillmentType.DELIVERY
                ? BigDecimal.valueOf(30000) : BigDecimal.ZERO;
        BigDecimal finalAmount = subTotal.add(shippingFee);
        try {
            var checkoutReq = new com.dawn.order.controller.request.CheckoutRequest(
                    req.idempotencyKey(), req.fulfillmentType(), req.shippingAddress(), null, null);
            OrderDto dto = orderCommandService.createOrder(checkoutReq, null,
                    req.guestEmail(), req.guestPhone(),
                    cartItems, stockItems, priceMap, subTotal, BigDecimal.ZERO, shippingFee, finalAmount);
            cartClient.clearGuestCart(req.guestToken());
            return dto;
        } catch (Exception e) {
            log.error("Guest checkout failed, compensating Redis", e);
            try {
                inventoryClient.incrementBatch(stockItems);
            } catch (Exception ce) {
                log.error("Compensate failed, reconciliation job will fix", ce);
            }
            throw e;
        }
    }
}
