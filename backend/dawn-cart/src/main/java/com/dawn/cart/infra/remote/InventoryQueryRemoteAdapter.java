package com.dawn.cart.infra.remote;

import com.dawn.common.contract.InventoryQueryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.services.mode", havingValue = "distributed")
public class InventoryQueryRemoteAdapter implements InventoryQueryPort {

    private final CartInventoryFeignClient feignClient;

    @Override
    public Integer getAvailableQuantity(Long skuId) {
        return feignClient.getAvailableQuantity(skuId);
    }
}
