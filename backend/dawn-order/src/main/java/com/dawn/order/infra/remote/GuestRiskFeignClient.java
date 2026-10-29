package com.dawn.order.infra.remote;

import com.dawn.common.feign.FeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "auth-service",
        contextId = "orderGuestRiskFeignClient",
        url = "${app.services.user-url:http://auth-service:8085}",
        path = "/api/v1/internal/risk/guest-phone",
        configuration = FeignClientConfig.class
)
public interface GuestRiskFeignClient {

    @GetMapping("/verified")
    boolean isPhoneVerified(@RequestParam("phone") String phone);

    @PostMapping("/consume")
    void consumePhoneVerification(@RequestParam("phone") String phone);

    @GetMapping("/abuse")
    Long phoneAbuseCount(@RequestParam("phone") String phone);

    @PostMapping("/abuse")
    void recordPhoneAbuse(@RequestParam("phone") String phone);
}
