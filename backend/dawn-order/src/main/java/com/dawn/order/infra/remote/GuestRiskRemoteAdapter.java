package com.dawn.order.infra.remote;

import com.dawn.order.port.out.GuestRiskPort;
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
    public boolean isPhoneVerified(String phone) {
        return feignClient.isPhoneVerified(phone);
    }

    @Override
    public void consumePhoneVerification(String phone) {
        feignClient.consumePhoneVerification(phone);
    }

    @Override
    public long phoneAbuseCount(String phone) {
        Long count = feignClient.phoneAbuseCount(phone);
        return count == null ? 0 : count;
    }

    @Override
    public void recordPhoneAbuse(String phone) {
        feignClient.recordPhoneAbuse(phone);
    }
}
