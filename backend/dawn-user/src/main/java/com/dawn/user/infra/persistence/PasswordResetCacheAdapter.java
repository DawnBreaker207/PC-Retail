package com.dawn.user.infra.persistence;

import com.dawn.user.port.out.PasswordResetPort;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class PasswordResetCacheAdapter implements PasswordResetPort {

    private final RedisTemplate<String, String> redisTemplate;

    @Value("${app.passwordResetExpirationMinutes:15}")
    private long expirationMinutes;

    private String key(String tokenHash) {
        return "pwd_reset:" + tokenHash;
    }

    @Override
    public void storeResetToken(String tokenHash, Long userId) {
        redisTemplate.opsForValue().set(
                key(tokenHash),
                userId.toString(),
                expirationMinutes, TimeUnit.MINUTES
        );
    }

    @Override
    public Long consumeResetToken(String tokenHash) {
        String k = key(tokenHash);
        String userId = redisTemplate.opsForValue().get(k);
        if (userId == null) return null;
        redisTemplate.delete(k);
        return Long.parseLong(userId);
    }
}
