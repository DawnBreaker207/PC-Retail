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
    private static final String ATTEMPT_PREFIX = "otp:attempts:";
    private static final long MAX_ATTEMPTS = 5;

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
    public String peekOtp(String email) {
        return redis.opsForValue().get(OTP_PREFIX + email);
    }

    @Override
    public long recordFailedAttempt(String email) {
        String key = ATTEMPT_PREFIX + email;
        Long count = redis.opsForValue().increment(key);
        if (count != null && count == 1) {
            redis.expire(key, Duration.ofMinutes(ttlMinutes));
        }
        return count == null ? MAX_ATTEMPTS : count;
    }

    @Override
    public void clearAttempts(String email) {
        redis.delete(ATTEMPT_PREFIX + email);
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
