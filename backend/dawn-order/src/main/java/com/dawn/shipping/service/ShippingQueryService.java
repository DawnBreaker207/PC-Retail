package com.dawn.shipping.service;

import com.dawn.common.exception.wrapper.ResourceNotFoundException;
import com.dawn.common.response.ResponsePage;
import com.dawn.shipping.dto.ShippingFeeRequest;
import com.dawn.shipping.dto.ShippingFeeResponse;
import com.dawn.shipping.dto.ShippingOrderDto;
import com.dawn.shipping.entity.ShippingOrder;
import com.dawn.shipping.port.in.QueryShipmentUseCase;
import com.dawn.shipping.port.out.ShippingOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class ShippingQueryService implements QueryShipmentUseCase {

    private final ShippingOrderRepository shippingOrderRepository;

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
