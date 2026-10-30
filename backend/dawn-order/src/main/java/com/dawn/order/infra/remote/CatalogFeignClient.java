package com.dawn.order.infra.remote;

import com.dawn.common.feign.FeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@FeignClient(
        name = "dawn-catalog",
        contextId = "orderCatalogFeignClient",
        url = "${app.services.catalog-url:http://dawn-catalog:8084}",
        path = "/api/v1/internal/catalog",
        configuration = FeignClientConfig.class
)
public interface CatalogFeignClient {

    @PostMapping("/prices")
    Map<Long, BigDecimal> prices(@RequestBody List<Long> skuIds);

    @PostMapping("/names")
    Map<Long, String> names(@RequestBody List<Long> skuIds);
}
