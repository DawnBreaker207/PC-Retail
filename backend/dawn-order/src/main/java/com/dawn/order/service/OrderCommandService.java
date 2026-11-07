package com.dawn.order.service;

import com.dawn.common.event.OrderCreatedEvent;
import com.dawn.common.outbox.OutboxPublisher;
import com.dawn.order.controller.request.CheckoutRequest;
import com.dawn.common.contract.CartItemSnapshot;
import com.dawn.order.dto.OrderDto;
import com.dawn.common.contract.StockDecrementCommand;
import com.dawn.order.entity.Order;
import com.dawn.order.entity.OrderItem;
import com.dawn.order.entity.SubOrder;
import com.dawn.order.entity.enums.PaymentMethod;
import com.dawn.order.entity.enums.PaymentStatus;
import com.dawn.order.entity.enums.SubOrderStatus;
import com.dawn.common.contract.InventoryClientPort;
import com.dawn.common.contract.PromotionClientPort;
import com.dawn.order.port.out.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderCommandService {

    private final OrderRepository orderRepository;
    private final SubOrderRepository subOrderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderQueryService orderQueryService;

    private final InventoryClientPort inventoryClient;
    private final PromotionClientPort promotionClient;

    private final ApplicationEventPublisher eventPublisher;
    private final OutboxPublisher outboxPublisher;

    public OrderDto createOrder(
            CheckoutRequest req,
            Long userId,
            List<CartItemSnapshot> cartItems,
            List<StockDecrementCommand> stockItems,
            Map<Long, BigDecimal> priceMap,
            BigDecimal subTotal,
            BigDecimal discountAmount,
            BigDecimal shippingFee,
            BigDecimal finalAmount) {
        return createOrder(req, userId, null, null, cartItems, stockItems,
                priceMap, subTotal, discountAmount, shippingFee, finalAmount);
    }

    public OrderDto createOrder(
            CheckoutRequest req,
            Long userId,
            String guestEmail,
            String guestPhone,
            List<CartItemSnapshot> cartItems,
            List<StockDecrementCommand> stockItems,
            Map<Long, BigDecimal> priceMap,
            BigDecimal subTotal,
            BigDecimal discountAmount,
            BigDecimal shippingFee,
            BigDecimal finalAmount) {
        // extract warehouseId from first stockItem (all items routed to same warehouse)
        Long warehouseId = stockItems.isEmpty() ? null : stockItems.get(0).warehouseId();

        PaymentMethod pm = req.resolvedPaymentMethod();
        // COD orders bypass payment gateway: paymentStatus = COD_PENDING
        PaymentStatus initialPaymentStatus = pm == PaymentMethod.COD
                ? PaymentStatus.COD_PENDING : PaymentStatus.PENDING;
        // COD orders skip PENDING_PAYMENT → go directly to PROCESSING
        SubOrderStatus initialSubStatus = pm == PaymentMethod.COD
                ? SubOrderStatus.PROCESSING : SubOrderStatus.PENDING_PAYMENT;

        Order order = Order.builder()
                .userId(userId)
                .guestEmail(guestEmail)
                .guestPhone(guestPhone)
                .shippingAddress(req.shippingAddress() != null
                        ? req.shippingAddress() : Map.of())
                .totalAmount(finalAmount)
                .taxAmount(BigDecimal.ZERO)
                .paymentStatus(initialPaymentStatus)
                .paymentMethod(pm)
                .reservedUntil(Instant.now().plusSeconds(120 * 60))
                .idempotencyKey(req.idempotencyKey())
                .taxInfo(req.taxInfo())
                .build();
        orderRepository.save(order);

        SubOrder subOrder = SubOrder.builder()
                .orderId(order.getId())
                .fulfillmentType(req.fulfillmentType())
                .subtotal(subTotal)
                .shippingFee(shippingFee)
                .discountAmount(discountAmount)
                .finalAmount(finalAmount)
                .warehouseId(warehouseId)
                .status(initialSubStatus)
                .statusHistory(new ArrayList<>(List.of(Map.of(
                        "from", "CREATED",
                        "to", initialSubStatus.name(),
                        "createdAt", Instant.now().toString()
                ))))
                .build();
        subOrderRepository.save(subOrder);

        // fail-fast if price missing; prevents zero-price orders when SKU is deleted
        List<OrderItem> orderItems = cartItems.stream()
                .map(item -> {
                    BigDecimal price = priceMap.get(item.skuId());
                    if (price == null) {
                        throw new com.dawn.common.exception.wrapper.InvalidRequestException(
                                com.dawn.common.message.Message.format(
                                        com.dawn.common.message.Message.Catalog.SKU_NOT_FOUND, item.skuId()));
                    }
                    return OrderItem.builder()
                            .subOrderId(subOrder.getId())
                            .skuId(item.skuId())
                            .quantity(item.quantity())
                            .unitPrice(price)
                            .build();
                })
                .collect(Collectors.toList());
        orderItemRepository.saveAll(orderItems);

        inventoryClient.reserve(stockItems, order.getId());

        if (req.voucherCode() != null && !req.voucherCode().isEmpty()) {
            promotionClient.recordUsage(
                    userId,
                    order.getId(),
                    List.of(req.voucherCode()),
                    order.getTotalAmount());
        }

        eventPublisher.publishEvent(new OrderCreatedEvent(order.getId(), userId));

        // Transactional Outbox: same transaction as order; relay publishes to Kafka after commit
        outboxPublisher.publish(
                "order-events",
                "ORDER",
                order.getId(),
                new OrderCreatedEvent(order.getId(), userId));

        return orderQueryService.toOrderDto(order);
    }
}
