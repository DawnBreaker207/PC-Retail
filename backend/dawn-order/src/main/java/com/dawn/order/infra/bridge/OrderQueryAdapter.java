package com.dawn.order.infra.bridge;

import com.dawn.order.dto.OrderQueryPort;
import com.dawn.order.port.out.OrderItemRepository;
import com.dawn.order.port.out.SubOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderQueryAdapter implements OrderQueryPort {
    private final OrderItemRepository orderItemRepository;
    private final SubOrderRepository subOrderRepository;

    @Override
    public boolean existsOrderItemBySkuId(Long skuId) {
        return orderItemRepository.existsBySkuId(skuId);
    }

    @Override
    public boolean isSubOrderDelivered(Long subOrderId) {
        return subOrderRepository.isDelivered(subOrderId);
    }
}
