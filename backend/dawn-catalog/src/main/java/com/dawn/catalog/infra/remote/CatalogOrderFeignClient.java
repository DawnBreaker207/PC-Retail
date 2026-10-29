package com.dawn.catalog.infra.remote;

import com.dawn.common.feign.FeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "order-service",
        contextId = "catalogOrderFeignClient",
        url = "${app.services.order-url:http://order-service:8081}",
        path = "/api/v1/internal/orders",
        configuration = FeignClientConfig.class
)
public interface CatalogOrderFeignClient {

    @GetMapping("/items/exists")
    boolean existsOrderItemBySkuId(@RequestParam("skuId") Long skuId);

    @GetMapping("/sub-orders/{subOrderId}/delivered")
    boolean isSubOrderDelivered(@PathVariable("subOrderId") Long subOrderId);
}
