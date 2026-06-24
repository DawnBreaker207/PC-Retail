package com.dawn.order.port.in;

import com.dawn.common.response.ResponsePage;
import com.dawn.order.dto.OrderDto;
import com.dawn.order.entity.Order;

public interface QueryOrderUseCase {
    ResponsePage<OrderDto> getMyOrders(Long userId, int page, int size);

    OrderDto getById(Long orderId, Long userId);

    OrderDto toOrderDto(Order order);
}
