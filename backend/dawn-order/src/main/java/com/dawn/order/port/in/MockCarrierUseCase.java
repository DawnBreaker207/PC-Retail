package com.dawn.order.port.in;

public interface MockCarrierUseCase {
    void assignCarrier(Long subOrderId, String carrierName);
    void mockDeliveryWebhook(Long subOrderId);
}
