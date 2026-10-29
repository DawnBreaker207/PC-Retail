package com.dawn.catalog.infra.remote;

import com.dawn.order.dto.OrderQueryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.services.mode", havingValue = "distributed")
public class OrderQueryRemoteAdapter implements OrderQueryPort {

    private final CatalogOrderFeignClient feignClient;

    @Override
    public boolean existsOrderItemBySkuId(Long skuId) {
        return feignClient.existsOrderItemBySkuId(skuId);
    }

    @Override
    public boolean isSubOrderDelivered(Long subOrderId) {
        return feignClient.isSubOrderDelivered(subOrderId);
    }
}
