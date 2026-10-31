package com.dawn.catalog.infra.remote;

import com.dawn.common.contract.OrderQueryPort;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.services.mode", havingValue = "distributed")
public class OrderQueryRemoteAdapter implements OrderQueryPort {

    private final CatalogOrderFeignClient feignClient;

    @Override
    @Bulkhead(name = "order")
    public boolean existsOrderItemBySkuId(Long skuId) {
        return feignClient.existsOrderItemBySkuId(skuId);
    }

    @Override
    @Bulkhead(name = "order")
    public boolean isSubOrderDelivered(Long subOrderId) {
        return feignClient.isSubOrderDelivered(subOrderId);
    }

    @Override
    @Bulkhead(name = "order")
    public boolean hasUserPurchasedSku(Long userId, Long skuId) {
        Map<String, Boolean> res = feignClient.verifyPurchase(userId, skuId);
        return res != null && Boolean.TRUE.equals(res.get("purchased"));
    }
}
