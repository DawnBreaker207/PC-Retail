package com.dawn.cart.infra.remote;

import com.dawn.common.feign.FeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "dawn-inventory",
        contextId = "cartInventoryFeignClient",
        url = "${app.services.inventory-url:http://dawn-inventory:8082}",
        path = "/api/v1/internal/inventory",
        configuration = FeignClientConfig.class
)
public interface CartInventoryFeignClient {

    @GetMapping("/{skuId}/available")
    Integer getAvailableQuantity(@PathVariable("skuId") Long skuId);
}
