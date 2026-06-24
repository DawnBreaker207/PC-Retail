package com.dawn.order.port.in;

import com.dawn.order.controller.request.CheckoutRequest;
import com.dawn.order.dto.OrderDto;

public interface CheckoutUseCase {
    OrderDto checkout(CheckoutRequest req, Long userId);
}
