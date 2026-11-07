package com.dawn.order.port.in;

import com.dawn.order.dto.SubOrderDto;
import com.dawn.order.entity.enums.SubOrderStatus;

public interface TransitionSubOrderUseCase {
    SubOrderDto transition(Long subOrderId, SubOrderStatus target, Long staffId);

    SubOrderDto cancel(Long subOrderId, Long userId);

    SubOrderDto assignShipper(Long subOrderId, Long shipperId, Long staffId);

    SubOrderDto deliver(Long subOrderId, Long shipperId, boolean codCollected);

    SubOrderDto deliveryFailed(Long subOrderId, Long shipperId, String note);

    void expiredUnpaidOrders();
}
