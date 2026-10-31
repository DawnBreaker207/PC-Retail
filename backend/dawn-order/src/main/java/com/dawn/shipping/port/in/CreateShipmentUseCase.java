package com.dawn.shipping.port.in;

import com.dawn.shipping.entity.ShippingOrder;

public interface CreateShipmentUseCase {

    ShippingOrder createShippingOrder(Long orderId, Long userId, String toAddress, Integer weightGrams);

    ShippingOrder updateStatus(String trackingCode, String newStatus);
}
