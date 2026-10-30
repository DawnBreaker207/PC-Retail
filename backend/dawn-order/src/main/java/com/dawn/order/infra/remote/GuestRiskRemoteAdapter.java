package com.dawn.order.infra.remote;

import com.dawn.common.contract.GuestRiskPort;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.services.mode", havingValue = "distributed")
public class GuestRiskRemoteAdapter implements GuestRiskPort {

    private final GuestRiskFeignClient feignClient;

    @Override
    @Bulkhead(name = "user")
    public boolean isPhoneVerified(String phone) {
        return feignClient.isPhoneVerified(phone);
    }

    @Override
    @Bulkhead(name = "user")
    public void consumePhoneVerification(String phone) {
        feignClient.consumePhoneVerification(phone);
    }

    @Override
    @Bulkhead(name = "user")
    public long phoneAbuseCount(String phone) {
        Long count = feignClient.phoneAbuseCount(phone);
        return count == null ? 0 : count;
    }

    @Override
    @Bulkhead(name = "user")
    public void recordPhoneAbuse(String phone) {
        feignClient.recordPhoneAbuse(phone);
    }
}
