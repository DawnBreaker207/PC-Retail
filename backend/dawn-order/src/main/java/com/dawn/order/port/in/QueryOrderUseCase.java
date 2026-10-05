package com.dawn.order.port.in;

import com.dawn.common.response.ResponsePage;
import com.dawn.order.dto.OrderDto;
import com.dawn.order.entity.Order;

import java.util.Optional;

public interface QueryOrderUseCase {
    ResponsePage<OrderDto> getMyOrders(Long userId, int page, int size);

    ResponsePage<OrderDto> getAllForStaff(int page, int size);

    OrderDto getByIdForStaff(Long orderId);

    OrderDto getById(Long orderId, Long userId);

    OrderDto toOrderDto(Order order);

    Optional<OrderDto> findByIdempotencyKey(String key);

    Optional<OrderDto> findByUserIdAndIdempotencyKey(Long userId, String key);
}
