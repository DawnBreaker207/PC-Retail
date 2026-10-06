package com.dawn.user.infra.redis;

import com.dawn.user.port.out.PhoneOtpPort;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class PhoneOtpCacheAdapter implements PhoneOtpPort {

    private static final String OTP_PREFIX = "otp:phone:";
    private static final String COOLDOWN_PREFIX = "otp:phone:cooldown:";
    private static final String ATTEMPT_PREFIX = "otp:phone:attempts:";
    private static final String VERIFIED_PREFIX = "otp:phone:verified:";
    private static final long MAX_ATTEMPTS = 5;

    private final StringRedisTemplate redis;

    @Value("${app.phone-otp.ttlMinutes:5}")
    private long ttlMinutes;

    @Value("${app.phone-otp.cooldownSeconds:60}")
    private long cooldownSeconds;

    @Value("${app.phone-otp.verifiedTtlMinutes:30}")
    private long verifiedTtlMinutes;

    @Override
    public void storeOtp(String phone, String otp) {
        redis.opsForValue().set(OTP_PREFIX + phone, otp, Duration.ofMinutes(ttlMinutes));
    }

    @Override
    public String peekOtp(String phone) {
        return redis.opsForValue().get(OTP_PREFIX + phone);
    }

    @Override
    public void consumeOtp(String phone) {
        redis.delete(OTP_PREFIX + phone);
    }

    @Override
    public long recordFailedAttempt(String phone) {
        String key = ATTEMPT_PREFIX + phone;
        Long count = redis.opsForValue().increment(key);
        if (count != null && count == 1) {
            redis.expire(key, Duration.ofMinutes(ttlMinutes));
        }
        return count == null ? MAX_ATTEMPTS : count;
    }

    @Override
    public void clearAttempts(String phone) {
        redis.delete(ATTEMPT_PREFIX + phone);
    }

    @Override
    public void storeCooldown(String phone) {
        redis.opsForValue().set(COOLDOWN_PREFIX + phone, "1", Duration.ofSeconds(cooldownSeconds));
    }

    @Override
    public boolean isOnCooldown(String phone) {
        return Boolean.TRUE.equals(redis.hasKey(COOLDOWN_PREFIX + phone));
    }

    @Override
    public void markVerified(String phone) {
        redis.opsForValue().set(VERIFIED_PREFIX + phone, "1", Duration.ofMinutes(verifiedTtlMinutes));
    }

    @Override
    public boolean isVerified(String phone) {
        return Boolean.TRUE.equals(redis.hasKey(VERIFIED_PREFIX + phone));
    }

    @Override
    public void consumeVerified(String phone) {
        redis.delete(VERIFIED_PREFIX + phone);
    }
}
