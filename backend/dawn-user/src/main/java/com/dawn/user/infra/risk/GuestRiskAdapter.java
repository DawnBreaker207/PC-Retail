package com.dawn.user.infra.risk;

import com.dawn.common.redis.service.RedisService;
import com.dawn.order.port.out.GuestRiskPort;
import com.dawn.user.port.out.PhoneOtpPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class GuestRiskAdapter implements GuestRiskPort {

    private static final String ABUSE_PREFIX = "risk:phone:abuse:";
    private static final Duration ABUSE_TTL = Duration.ofDays(90);

    private final PhoneOtpPort phoneOtpPort;
    private final RedisService redisService;

    @Override
    public boolean isPhoneVerified(String phone) {
        return phone != null && phoneOtpPort.isVerified(phone);
    }

    @Override
    public void consumePhoneVerification(String phone) {
        if (phone != null) phoneOtpPort.consumeVerified(phone);
    }

    @Override
    public long phoneAbuseCount(String phone) {
        if (phone == null) return 0;
        Object val = redisService.get(ABUSE_PREFIX + phone);
        if (val == null) return 0;
        try {
            return Long.parseLong(val.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    @Override
    public void recordPhoneAbuse(String phone) {
        if (phone == null) return;
        String key = ABUSE_PREFIX + phone;
        Long count = redisService.increment(key, 1L);
        if (count != null && count == 1L) {
            redisService.expire(key, ABUSE_TTL);
        }
    }
}
