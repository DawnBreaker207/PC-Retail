package com.dawn.order.service;

import com.dawn.common.exception.wrapper.InvalidRequestException;
import com.dawn.common.message.Message;
import com.dawn.order.controller.request.CheckoutRequest;
import com.dawn.order.dto.*;
import com.dawn.order.entity.Order;
import com.dawn.order.entity.OrderItem;
import com.dawn.order.entity.SubOrder;
import com.dawn.order.entity.enums.FulfillmentType;
import com.dawn.order.entity.enums.PaymentStatus;
import com.dawn.order.entity.enums.SubOrderStatus;
import com.dawn.order.port.in.CheckoutUseCase;
import com.dawn.order.port.out.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CheckoutService implements CheckoutUseCase {
    private static final String VOUCHER_KEY = "voucher:remaining";
    private static final BigDecimal DELIVERY_FEE = BigDecimal.valueOf(30000);

    private final OrderRepositoryPort orderRepository;
    private final SubOrderRepositoryPort subOrderRepository;
    private final OrderItemRepositoryPort orderItemRepository;
    private final OrderQueryService orderQueryService;

    private final CartClientPort cartClient;
    private final InventoryClientPort inventoryClient;
    private final PromotionClientPort promotionClient;
    private final CatalogClientPort catalogClient;

    @Override
    @Transactional
    public OrderDto checkout(CheckoutRequest req, Long userId) {
        // Step 1: Idempotency
        var existing = orderRepository.findByIdempotencyKey(req.idempotencyKey());
        if (existing.isPresent()) {
            return orderQueryService.toOrderDto(existing.get());
        }
        // Step 2: Load + Validate cart
        List<CartItemSnapshot> cartItems = cartClient.getItems(userId);
        if (cartItems.isEmpty()) {
            throw new InvalidRequestException(Message.Order.CART_EMPTY);
        }

        if (req.fulfillmentType() == FulfillmentType.DELIVERY && (req.shippingAddress() == null || req.shippingAddress().isEmpty())) {
            throw new InvalidRequestException(Message.Order.SHIPPING_ADDRESS_REQUIRED);
        }

        List<Long> skuIds = cartItems.stream().map(CartItemSnapshot::skuId).toList();

        Map<Long, BigDecimal> priceMap = catalogClient.getPrices(skuIds);
        BigDecimal subTotal = cartItems.stream()
                .map(item -> priceMap
                        .getOrDefault(item.skuId(), BigDecimal.ZERO)
                        .multiply(BigDecimal.valueOf(item.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<String> voucherCodes = req.voucherCode() != null ? List.of(req.voucherCode()) : List.of();
        BigDecimal discountAmount = BigDecimal.ZERO;

        if (!voucherCodes.isEmpty()) {
            promotionClient.validateStack(skuIds, voucherCodes);
            VoucherApplyResult validation = promotionClient.validate(req.voucherCode(), subTotal);
            if (!validation.valid()) {
                throw new InvalidRequestException(validation.errorMessage());
            }
            discountAmount = validation.discountAmount();
        }

        if (!voucherCodes.isEmpty()) {
            promotionClient.reservePromotions(voucherCodes, List.of());
        }

        List<StockDecrementCommand> stockItems = cartItems.stream()
                .map(item -> new StockDecrementCommand(item.skuId(), item.quantity()))
                .toList();

        List<Long> failedSkuIds = inventoryClient.decrementBatch(stockItems);
        if (!failedSkuIds.isEmpty()) {
            if (!voucherCodes.isEmpty()) promotionClient.rollBackPromotions(voucherCodes, List.of());
            throw new InvalidRequestException(Message.format(Message.Order.INSUFFICIENT_STOCK_SKUS, failedSkuIds));
        }

        try {
            BigDecimal shippingFee = req.fulfillmentType() == FulfillmentType.DELIVERY ? DELIVERY_FEE : BigDecimal.ZERO;
            BigDecimal finalAmount = subTotal.subtract(discountAmount).add(shippingFee);

            // Create Order
            Order order = Order.builder()
                    .userId(userId)
                    .shippingAddress(req.shippingAddress() != null ? req.shippingAddress() : Map.of())
                    .totalAmount(finalAmount)
                    .taxAmount(BigDecimal.ZERO)
                    .paymentStatus(PaymentStatus.PENDING)
                    .reservedUntil(Instant.now().plusSeconds(15 * 60))
                    .idempotencyKey(req.idempotencyKey())
                    .taxInfo(req.taxInfo())
                    .build();
            orderRepository.save(order);

            // Create SubOrder
            SubOrder subOrder = SubOrder.builder()
                    .orderId(order.getId())
                    .fulfillmentType(req.fulfillmentType())
                    .subtotal(subTotal)
                    .shippingFee(shippingFee)
                    .discountAmount(discountAmount)
                    .finalAmount(finalAmount)
                    .status(SubOrderStatus.PENDING_PAYMENT)
                    .statusHistory(new ArrayList<>(List.of(Map.of(
                            "from", "CREATED",
                            "to", SubOrderStatus.PENDING_PAYMENT.name(),
                            "createdAt", Instant.now().toString()
                    ))))
                    .build();
            subOrderRepository.save(subOrder);

            // Create OrderItems
            List<OrderItem> orderItems = cartItems.stream()
                    .map(item -> OrderItem.builder()
                            .subOrderId(subOrder.getId())
                            .skuId(item.skuId())
                            .quantity(item.quantity())
                            .unitPrice(priceMap.getOrDefault(item.skuId(), BigDecimal.ZERO))
                            .build())
                    .collect(Collectors.toList());
            orderItemRepository.saveAll(orderItems);

            // Reserve inventory with pessimistic lock
            inventoryClient.reserve(stockItems, order.getId());

            // Voucher usage
            if (!voucherCodes.isEmpty()) {
                promotionClient.recordUsage(userId, order.getId(), voucherCodes);
            }

            // Step 7: AFTER COMMIT - Clear Cart
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    cartClient.clearCart(userId);
                }
            });

            return orderQueryService.toOrderDto(order);
        } catch (DataIntegrityViolationException dup) {
            log.warn("Duplicate checkout DB constraint, key={}", req.idempotencyKey());
            inventoryClient.incrementBatch(stockItems);
            if (!voucherCodes.isEmpty()) promotionClient.rollBackPromotions(voucherCodes, List.of());
            return orderRepository.findByIdempotencyKey(req.idempotencyKey())
                    .map(orderQueryService::toOrderDto)
                    .orElseThrow(() -> dup);
        } catch (Exception e) {
            log.error("Checkout DB transaction failed, compensating Redis", e);
            inventoryClient.incrementBatch(stockItems);
            if (!voucherCodes.isEmpty()) promotionClient.rollBackPromotions(voucherCodes, List.of());
            throw e;
        }
    }
}
