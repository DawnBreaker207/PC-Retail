package com.dawn.cart.infra.remote;

import com.dawn.common.contract.SkuPriceInfo;
import com.dawn.common.feign.FeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

@FeignClient(
        name = "dawn-catalog",
        contextId = "cartCatalogFeignClient",
        url = "${app.services.catalog-url:http://dawn-catalog:8084}",
        path = "/api/v1/internal/catalog",
        configuration = FeignClientConfig.class
)
public interface CartCatalogFeignClient {

    @PostMapping("/details")
    Map<Long, SkuPriceInfo> details(@RequestBody List<Long> skuIds);
}
