package com.dawn.order.port.in;

import com.dawn.order.dto.SubOrderDto;
import com.dawn.order.entity.enums.SubOrderStatus;

public interface TransitionSubOrderUseCase {
    SubOrderDto transition(Long subOrderId, SubOrderStatus target, Long staffId);

    SubOrderDto cancel(Long subOrderId, Long userId);

    void expiredUnpaidOrders();
}
