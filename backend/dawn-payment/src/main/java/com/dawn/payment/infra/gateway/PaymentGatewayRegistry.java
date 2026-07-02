package com.dawn.payment.infra.gateway;

import com.dawn.common.message.Message;
import com.dawn.payment.entity.enums.PaymentMethod;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class PaymentGatewayRegistry {
    private final Map<PaymentMethod, PaymentGateway> gateways;

    public PaymentGatewayRegistry(List<PaymentGateway> gatewayList) {
        this.gateways = new EnumMap<>(PaymentMethod.class);
        for (PaymentGateway g : gatewayList) {
            gateways.put(g.supportedMethod(), g);
        }
    }

    public PaymentGateway get(PaymentMethod method) {
        PaymentGateway gateway = gateways.get(method);
        if (gateway == null) {
            throw new IllegalArgumentException(Message.format(Message.Payment.GATEWAY_NOT_FOUND, method));
        }
        return gateway;
    }
}
