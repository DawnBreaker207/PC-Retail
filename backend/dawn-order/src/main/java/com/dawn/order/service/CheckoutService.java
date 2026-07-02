package com.dawn.order.service;

import com.dawn.common.exception.wrapper.InvalidRequestException;
import com.dawn.common.message.Message;
import com.dawn.order.controller.request.CheckoutRequest;
import com.dawn.order.dto.CartItemSnapshot;
import com.dawn.order.dto.OrderDto;
import com.dawn.order.dto.StockDecrementCommand;
import com.dawn.order.dto.VoucherApplyResult;
import com.dawn.order.entity.enums.FulfillmentType;
import com.dawn.order.port.in.CheckoutUseCase;
import com.dawn.order.port.out.CartClientPort;
import com.dawn.order.port.out.CatalogClientPort;
import com.dawn.order.port.out.InventoryClientPort;
import com.dawn.order.port.out.PromotionClientPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

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

    @Override
    public OrderDto checkout(CheckoutRequest req, Long userId) {
        // Step 0: Idempotency
        var existing = orderQueryService.findByIdempotencyKey(req.idempotencyKey());
        if (existing.isPresent()) return existing.get();

        // Step 1: Load + Validate cart
        List<CartItemSnapshot> cartItems = cartClient.getItems(userId);
        if (cartItems.isEmpty()) {
            throw new InvalidRequestException(Message.Order.CART_EMPTY);
        }

        if (req.fulfillmentType() == FulfillmentType.DELIVERY
                && (req.shippingAddress() == null || req.shippingAddress().isEmpty())) {
            throw new InvalidRequestException(Message.Order.SHIPPING_ADDRESS_REQUIRED);
        }

        // Step 2: Calculate Amount
        List<Long> skuIds = cartItems
                .stream()
                .map(CartItemSnapshot::skuId)
                .toList();
        Map<Long, BigDecimal> priceMap = catalogClient.getPrices(skuIds);
        BigDecimal subTotal = cartItems.stream()
                .map(item -> priceMap
                        .getOrDefault(item.skuId(), BigDecimal.ZERO)
                        .multiply(BigDecimal.valueOf(item.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Step 3: Validate Voucher
        List<String> voucherCodes = req.voucherCode() != null
                ? List.of(req.voucherCode()) : List.of();
        BigDecimal discountAmount = BigDecimal.ZERO;

        if (!voucherCodes.isEmpty()) {
            promotionClient.validateStack(skuIds, voucherCodes);
            VoucherApplyResult validation = promotionClient.validate(req.voucherCode(), subTotal);
            if (!validation.valid()) {
                throw new InvalidRequestException(validation.errorMessage());
            }
            discountAmount = validation.discountAmount();
        }

        List<StockDecrementCommand> stockItems = cartItems.stream()
                .map(item -> new StockDecrementCommand(item.skuId(), item.quantity()))
                .toList();


        // Phase 1: Lua gatekeeper (Out of transaction)
        List<Long> failedSkuIds = inventoryClient.decrementBatch(stockItems);
        if (!failedSkuIds.isEmpty())
            throw new InvalidRequestException(Message.format(Message.Order.INSUFFICIENT_STOCK_SKUS, failedSkuIds));


        if (!voucherCodes.isEmpty())
            promotionClient.reservePromotions(voucherCodes, List.of());

        // Phage 2: DB Transaction
        BigDecimal shippingFee = req.fulfillmentType() == FulfillmentType.DELIVERY ? DELIVERY_FEE : BigDecimal.ZERO;
        BigDecimal finalAmount = subTotal.subtract(discountAmount).add(shippingFee);
        try {
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
                    .findByIdempotencyKey(req.idempotencyKey())
                    .orElseThrow(() -> dup);
        } catch (Exception e) {
            log.error("Checkout DB transaction failed, compensating Redis", e);
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
}
