package com.dawn.shipping.service;

import com.dawn.common.exception.wrapper.ResourceNotFoundException;
import com.dawn.common.outbox.OutboxPublisher;
import com.dawn.common.response.ResponsePage;
import com.dawn.shipping.dto.ShippingFeeRequest;
import com.dawn.shipping.dto.ShippingFeeResponse;
import com.dawn.shipping.dto.ShippingOrderDto;
import com.dawn.shipping.entity.ShippingOrder;
import com.dawn.shipping.entity.enums.Carrier;
import com.dawn.shipping.entity.enums.ShippingStatus;
import com.dawn.shipping.repository.ShippingOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShippingService {

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

        outboxPublisher.publish("shipping-events", String.valueOf(orderId), Map.of(
                "type", "SHIPPING_CREATED",
                "orderId", orderId,
                "shippingId", order.getId(),
                "shippingFee", fee,
                "estimatedDelivery", estimatedDelivery.toString()
        ));

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

        outboxPublisher.publish("shipping-events", String.valueOf(order.getOrderId()), Map.of(
                "type", "SHIPPING_STATUS_UPDATED",
                "orderId", order.getOrderId(),
                "trackingCode", trackingCode,
                "status", newStatus
        ));

        log.info("Updated shipping status: trackingCode={}, status={}", trackingCode, newStatus);
        return order;
    }

    @Transactional(readOnly = true)
    public ShippingFeeResponse calculateFee(ShippingFeeRequest req) {
        int weight = req.weightGrams() != null ? req.weightGrams() : 500;
        BigDecimal fee = calculateShippingFee(weight);
        return new ShippingFeeResponse("MOCK", fee, 3);
    }

    @Transactional(readOnly = true)
    public ShippingOrderDto getByOrderId(Long orderId) {
        ShippingOrder order = shippingOrderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipping not found for order: " + orderId));
        return toDto(order);
    }

    @Transactional(readOnly = true)
    public ResponsePage<ShippingOrderDto> listByUser(Long userId, Pageable pageable) {
        Page<ShippingOrder> page = shippingOrderRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return ResponsePage.of(page.map(this::toDto));
    }

    private BigDecimal calculateShippingFee(Integer weightGrams) {
        if (weightGrams == null || weightGrams <= 0) weightGrams = 500;
        long units = (long) Math.ceil(weightGrams / 500.0);
        return BigDecimal.valueOf(25000L + (units - 1) * 5000L);
    }

    private ShippingOrderDto toDto(ShippingOrder o) {
        return new ShippingOrderDto(
                o.getId(), o.getOrderId(), o.getTrackingCode(),
                o.getCarrier() != null ? o.getCarrier().name() : null,
                o.getStatus() != null ? o.getStatus().name() : null,
                o.getShippingFee(), o.getEstimatedDelivery(), o.getCreatedAt()
        );
    }
}
