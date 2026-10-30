package com.dawn.auth.service;

import com.dawn.auth.dto.TokenResponse;
import com.dawn.auth.entity.RefreshToken;
import com.dawn.auth.entity.UserCredential;
import com.dawn.auth.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class TokenService {

    private static final String BLACKLIST_PREFIX = "jwt:blacklist:";

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtIssuerService jwtIssuerService;
    private final StringRedisTemplate redis;

    @Value("${app.jwtRefreshExpirationsMs}")
    private long refreshExpirationMs;

    @Transactional
    public TokenResponse issue(UserCredential cred) {
        String accessToken = jwtIssuerService.issue(
                cred.getUserId(), cred.getEmail(), Arrays.asList(cred.getRoles()));

        String rawRefresh = generateRaw();
        String tokenHash = sha256(rawRefresh);

        refreshTokenRepository.save(RefreshToken.builder()
                .userId(cred.getUserId())
                .tokenHash(tokenHash)
                .expiresAt(Instant.now().plusMillis(refreshExpirationMs))
                .build());

        return new TokenResponse(accessToken, rawRefresh, jwtIssuerService.expirationMs() / 1000);
    }

    @Transactional
    public TokenResponse rotate(String rawRefreshToken, UserCredential cred) {
        String hash = sha256(rawRefreshToken);
        RefreshToken stored = refreshTokenRepository.findByTokenHash(hash)
                .filter(t -> !t.isRevoked())
                .filter(t -> t.getExpiresAt().isAfter(Instant.now()))
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired refresh token"));

        stored.setRevoked(true);
        refreshTokenRepository.save(stored);

        return issue(cred);
    }

    @Transactional
    public void revokeAll(Long userId) {
        refreshTokenRepository.revokeAllByUserId(userId);
    }

    public void blacklistAccessToken(String token, long expiresInSeconds) {
        redis.opsForValue().set(BLACKLIST_PREFIX + token, "1", expiresInSeconds, TimeUnit.SECONDS);
    }

    public boolean isBlacklisted(String token) {
        return Boolean.TRUE.equals(redis.hasKey(BLACKLIST_PREFIX + token));
    }

    public java.util.Optional<RefreshToken> findByHash(String hash) {
        return refreshTokenRepository.findByTokenHash(hash)
                .filter(t -> !t.isRevoked())
                .filter(t -> t.getExpiresAt().isAfter(Instant.now()));
    }

    private String generateRaw() {
        byte[] bytes = new byte[48];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("SHA-256 unavailable", e);
        }
    }

    private List<String> listOf(String[] arr) {
        return arr == null ? List.of("ROLE_USER") : Arrays.asList(arr);
    }
}
