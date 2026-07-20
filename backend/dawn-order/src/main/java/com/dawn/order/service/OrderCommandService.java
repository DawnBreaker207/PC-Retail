package com.dawn.order.service;

import com.dawn.common.event.OrderCreatedEvent;
import com.dawn.order.controller.request.CheckoutRequest;
import com.dawn.order.dto.CartItemSnapshot;
import com.dawn.order.dto.OrderDto;
import com.dawn.order.dto.StockDecrementCommand;
import com.dawn.order.entity.Order;
import com.dawn.order.entity.OrderItem;
import com.dawn.order.entity.SubOrder;
import com.dawn.order.entity.enums.PaymentStatus;
import com.dawn.order.entity.enums.SubOrderStatus;
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

        // Create Order
        Order order = Order.builder()
                .userId(userId)
                .shippingAddress(req.shippingAddress() != null
                        ? req.shippingAddress() : Map.of())
                .totalAmount(finalAmount)
                .taxAmount(BigDecimal.ZERO)
                .paymentStatus(PaymentStatus.PENDING)
                .reservedUntil(Instant.now().plusSeconds(120 * 60))
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

        // Reserve inventory
        inventoryClient.reserve(stockItems, order.getId());

        // Voucher usage
        if (req.voucherCode() != null && !req.voucherCode().isEmpty()) {
            promotionClient.recordUsage(
                    userId,
                    order.getId(),
                    List.of(req.voucherCode()),
                    order.getTotalAmount());
        }

        // Publish cart cleanup event
        eventPublisher.publishEvent(new OrderCreatedEvent(order.getId(), userId));

        return orderQueryService.toOrderDto(order);
    }
}
