package com.dawn.cart.infra.remote;

import com.dawn.common.feign.FeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "inventory-service",
        contextId = "cartInventoryFeignClient",
        url = "${app.services.inventory-url:http://inventory-service:8082}",
        path = "/api/v1/internal/inventory",
        configuration = FeignClientConfig.class
)
public interface CartInventoryFeignClient {

    @GetMapping("/{skuId}/available")
    Integer getAvailableQuantity(@PathVariable("skuId") Long skuId);
}
