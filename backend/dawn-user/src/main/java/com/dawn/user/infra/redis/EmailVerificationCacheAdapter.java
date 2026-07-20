package com.dawn.user.infra.redis;

import com.dawn.user.port.out.EmailVerificationCachePort;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class EmailVerificationCacheAdapter implements EmailVerificationCachePort {

    private static final String OTP_PREFIX = "otp:register:";
    private static final String COOLDOWN_PREFIX = "otp:cooldown:";

    private final StringRedisTemplate redis;

    @Value("${app.otp.ttlMinutes:5}")
    private long ttlMinutes;

    @Value("${app.otp.cooldownSeconds:60}")
    private long cooldownSeconds;

    @Override
    public void storeOtp(String email, String otp) {
        redis.opsForValue().set(OTP_PREFIX + email, otp, Duration.ofMinutes(ttlMinutes));
    }

    @Override
    public String consumeOtp(String email) {
        String key = OTP_PREFIX + email;
        String otp = redis.opsForValue().get(key);
        if (otp != null) redis.delete(key);
        return otp;
    }

    @Override
    public void storeCooldown(String email) {
        redis.opsForValue().set(
                COOLDOWN_PREFIX + email, "1",
                Duration.ofSeconds(cooldownSeconds));
    }

    @Override
    public boolean isOnCooldown(String email) {
        return Boolean.TRUE.equals(
                redis.hasKey(COOLDOWN_PREFIX + email));
    }
}
