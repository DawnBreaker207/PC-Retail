package com.dawn.shipping.service;

import com.dawn.common.event.ShippingCreatedEvent;
import com.dawn.common.event.ShippingStatusUpdatedEvent;
import com.dawn.common.exception.wrapper.ResourceNotFoundException;
import com.dawn.common.outbox.OutboxPublisher;
import com.dawn.shipping.entity.ShippingOrder;
import com.dawn.shipping.entity.enums.Carrier;
import com.dawn.shipping.entity.enums.ShippingStatus;
import com.dawn.shipping.port.in.CreateShipmentUseCase;
import com.dawn.shipping.port.out.ShippingOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShippingCommandService implements CreateShipmentUseCase {

    private final ShippingOrderRepository shippingOrderRepository;
    private final OutboxPublisher outboxPublisher;

    @Transactional
    public ShippingOrder createShippingOrder(Long orderId, Long userId, String toAddress, Integer weightGrams) {
        BigDecimal fee = calculateShippingFee(weightGrams);
        Instant estimatedDelivery = Instant.now().plus(3, ChronoUnit.DAYS);

        ShippingOrder order = ShippingOrder.builder()
                .orderId(orderId)
                .userId(userId)
                .toAddress(toAddress)
                .weightGrams(weightGrams)
                .carrier(Carrier.MOCK)
                .status(ShippingStatus.PENDING)
                .shippingFee(fee)
                .estimatedDelivery(estimatedDelivery)
                .build();

        shippingOrderRepository.save(order);

        outboxPublisher.publish("shipping-events", "ShippingOrder", orderId,
                new ShippingCreatedEvent(orderId, order.getId(), fee, estimatedDelivery));

        log.info("Created shipping order {} for orderId={}", order.getId(), orderId);
        return order;
    }

    @Transactional
    public ShippingOrder updateStatus(String trackingCode, String newStatus) {
        ShippingOrder order = shippingOrderRepository.findByTrackingCode(trackingCode)
                .orElseThrow(() -> new ResourceNotFoundException("Shipping order not found: " + trackingCode));

        ShippingStatus status = ShippingStatus.valueOf(newStatus.toUpperCase());
        order.setStatus(status);
        shippingOrderRepository.save(order);

        outboxPublisher.publish("shipping-events", "ShippingOrder", order.getOrderId(),
                new ShippingStatusUpdatedEvent(order.getOrderId(), trackingCode, newStatus));

        log.info("Updated shipping status: trackingCode={}, status={}", trackingCode, newStatus);
        return order;
    }

    private BigDecimal calculateShippingFee(Integer weightGrams) {
        if (weightGrams == null || weightGrams <= 0) weightGrams = 500;
        long units = (long) Math.ceil(weightGrams / 500.0);
        return BigDecimal.valueOf(25000L + (units - 1) * 5000L);
    }
}
