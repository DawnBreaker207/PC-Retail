package com.dawn.order.service;

import com.dawn.common.exception.wrapper.ResourceNotFoundException;
import com.dawn.common.message.Message;
import com.dawn.order.entity.SubOrder;
import com.dawn.order.entity.enums.SubOrderStatus;
import com.dawn.order.port.in.MockCarrierUseCase;
import com.dawn.order.port.out.SubOrderRepository;
import com.dawn.order.statemachine.SubOrderStateMachine;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MockCarrierService implements MockCarrierUseCase {
    private final SubOrderRepository subOrderRepository;
    private final SubOrderStateMachine stateMachine;

    @Transactional
    public void assignCarrier(Long subOrderId, String carrierName) {
        SubOrder subOrder = subOrderRepository.findById(subOrderId).orElseThrow(() -> new ResourceNotFoundException(Message.format(Message.Order.SUB_ORDER_NOT_FOUND, subOrderId)));

        stateMachine.assertValidTransition(subOrder.getStatus(), SubOrderStatus.SHIPPED);

        subOrder.setTrackingCode("PC-" + subOrderId + "-" + Instant.now().toEpochMilli());
        subOrder.setCarrier(Map.of(
                "name", carrierName,
                "assignedAt", Instant.now().toString()));

        subOrder.setStatus(SubOrderStatus.SHIPPED);
        subOrderRepository.save(subOrder);
    }

    @Transactional
    public void mockDeliveryWebhook(Long subOrderId) {
        SubOrder subOrder = subOrderRepository.findById(subOrderId)
                .orElseThrow(() -> new ResourceNotFoundException(Message.format(Message.Order.SUB_ORDER_NOT_FOUND, subOrderId)));

        stateMachine.assertValidTransition(subOrder.getStatus(), SubOrderStatus.DELIVERED);

        subOrder.setWebhookLog(Map.of(
                "deliveredAt", Instant.now().toString(),
                "note", "Mock delivery by staff"
        ));
        subOrder.setStatus(SubOrderStatus.DELIVERED);
        subOrderRepository.save(subOrder);

    }
}
