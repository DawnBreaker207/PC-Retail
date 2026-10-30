package com.dawn.rating.infra.remote;

import com.dawn.common.feign.FeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(
        name = "dawn-order",
        url = "${app.services.order-url:http://localhost:8081}",
        path = "/api/v1/internal/orders",
        configuration = FeignClientConfig.class
)
public interface OrderVerifyFeignClient {

    @GetMapping("/verify-purchase")
    Map<String, Boolean> verifyPurchase(
            @RequestParam("userId") Long userId,
            @RequestParam("skuId") Long skuId
    );
}
