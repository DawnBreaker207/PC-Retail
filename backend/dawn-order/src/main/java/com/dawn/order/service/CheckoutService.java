package com.dawn.order.service;

import com.dawn.common.exception.wrapper.InvalidRequestException;
import com.dawn.common.message.Message;
import com.dawn.order.controller.request.CheckoutRequest;
import com.dawn.common.contract.CartItemSnapshot;
import com.dawn.order.dto.OrderDto;
import com.dawn.common.contract.StockDecrementCommand;
import com.dawn.common.contract.VoucherApplyResult;
import com.dawn.order.entity.enums.FulfillmentType;
import com.dawn.order.port.in.CheckoutUseCase;
import com.dawn.common.contract.CartClientPort;
import com.dawn.common.contract.CatalogClientPort;
import com.dawn.common.contract.InventoryClientPort;
import com.dawn.common.contract.PromotionClientPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class CheckoutService implements CheckoutUseCase {
    private static final BigDecimal DELIVERY_FEE = BigDecimal.valueOf(30000);

    private final OrderCommandService orderCommandService;
    private final OrderQueryService orderQueryService;

    private final CartClientPort cartClient;
    private final InventoryClientPort inventoryClient;
    private final PromotionClientPort promotionClient;
    private final CatalogClientPort catalogClient;
    private final ThreadPoolTaskExecutor checkoutExecutor;
    private final ShippingService shippingService;

    @Override
    public OrderDto checkout(CheckoutRequest req, Long userId) {
        // scope by owner to prevent order enumeration via idempotency key
        var existing = orderQueryService.findByUserIdAndIdempotencyKey(userId, req.idempotencyKey());
        if (existing.isPresent()) return existing.get();

        List<CartItemSnapshot> cartItems = cartClient.getItems(userId);
        if (cartItems.isEmpty()) {
            throw new InvalidRequestException(Message.Order.CART_EMPTY);
        }

        if (req.fulfillmentType() == FulfillmentType.DELIVERY
                && (req.shippingAddress() == null || req.shippingAddress().isEmpty())) {
            throw new InvalidRequestException(Message.Order.SHIPPING_ADDRESS_REQUIRED);
        }

        List<Long> skuIds = cartItems
                .stream()
                .map(CartItemSnapshot::skuId)
                .toList();

        CompletableFuture<Map<Long, BigDecimal>> pricesFuture = CompletableFuture
                .supplyAsync(() -> catalogClient.getPrices(skuIds), checkoutExecutor);

        List<String> voucherCodes = req.voucherCode() != null
                ? List.of(req.voucherCode()) : List.of();

        Map<Long, BigDecimal> priceMap = joinUnchecked(pricesFuture);
        BigDecimal subTotal = cartItems.stream()
                .map(item -> priceMap
                        .getOrDefault(item.skuId(), BigDecimal.ZERO)
                        .multiply(BigDecimal.valueOf(item.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Validate voucher after price resolution; discount amount used for order creation
        BigDecimal discountAmount = BigDecimal.ZERO;
        if (!voucherCodes.isEmpty()) {
            promotionClient.validateStack(skuIds, voucherCodes);
            VoucherApplyResult validation = promotionClient.validate(req.voucherCode(), subTotal);
            if (!validation.valid()) {
                throw new InvalidRequestException(validation.errorMessage());
            }
            discountAmount = validation.discountAmount();
        }

        List<StockDecrementCommand> baseItems = cartItems.stream()
                .map(item -> new StockDecrementCommand(item.skuId(), item.quantity()))
                .toList();

        // Route to warehouse with enough stock, then attach warehouseId to each command
        Long warehouseId = inventoryClient.routeWarehouse(baseItems);
        List<StockDecrementCommand> stockItems = baseItems.stream()
                .map(item -> new StockDecrementCommand(item.skuId(), item.quantity(), warehouseId))
                .toList();

        // Phase 1: Lua atomic decrement, outside transaction
        List<Long> failedSkuIds = inventoryClient.decrementBatch(stockItems);
        if (!failedSkuIds.isEmpty())
            throw new InvalidRequestException(Message.format(Message.Order.INSUFFICIENT_STOCK_SKUS, failedSkuIds));

        // Phase 2: reserve voucher + DB transaction in same try-catch so compensation
        // runs on any failure after stock has been decremented in Redis
        BigDecimal shippingFee = shippingService.resolveFee(req.fulfillmentType(), req.shippingAddress());
        BigDecimal finalAmount = subTotal.subtract(discountAmount).add(shippingFee);
        try {
            if (!voucherCodes.isEmpty())
                promotionClient.reservePromotions(voucherCodes, List.of());

            return orderCommandService.createOrder(
                    req,
                    userId,
                    cartItems,
                    stockItems,
                    priceMap,
                    subTotal,
                    discountAmount,
                    shippingFee,
                    finalAmount);

        } catch (DataIntegrityViolationException dup) {
            log.warn("Duplicate checkout DB constraint, key={}", req.idempotencyKey());
            compensate(stockItems, voucherCodes);
            return orderQueryService
                    .findByUserIdAndIdempotencyKey(userId, req.idempotencyKey())
                    .orElseThrow(() -> dup);
        } catch (Exception e) {
            log.error("Checkout failed after stock decrement, compensating Redis", e);
            compensate(stockItems, voucherCodes);
            throw e;
        }
    }

    private void compensate(List<StockDecrementCommand> items, List<String> voucherCodes) {
        try {
            inventoryClient.incrementBatch(items);
            if (!voucherCodes.isEmpty())
                promotionClient.rollBackPromotions(voucherCodes, List.of());
        } catch (Exception e) {
            log.error("Compensate failed, reconciliation job will fix", e);
        }
    }

    /** Unwraps CompletionException so the original error code (400/409) propagates instead of 500. */
    private static <T> T joinUnchecked(CompletableFuture<T> future) {
        try {
            return future.join();
        } catch (java.util.concurrent.CompletionException e) {
            if (e.getCause() instanceof RuntimeException re) throw re;
            throw e;
        }
    }
}
