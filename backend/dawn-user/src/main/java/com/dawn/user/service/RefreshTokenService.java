package com.dawn.user.service;

import com.dawn.common.exception.wrapper.UnauthorizedException;
import com.dawn.common.message.Message;
import com.dawn.common.utils.JWTUtils;
import com.dawn.user.controller.response.AuthResponse;
import com.dawn.user.dto.UserMapper;
import com.dawn.user.entity.RefreshToken;
import com.dawn.user.entity.User;
import com.dawn.user.infra.persistence.RefreshTokenRepository;
import com.dawn.user.infra.persistence.JpaUserRepository;
import com.dawn.user.entity.enums.UserStatus;
import com.dawn.user.util.TokenUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JpaUserRepository userRepository;
    private final JWTUtils jwtUtils;

    @Value("${app.jwtRefreshExpirationsMs:604800000}")
    private long refreshTokenExpirationMs;

    @Transactional
    public String createRefreshToken(User user) {
        String rawToken = TokenUtils.generateRefreshToken();
        RefreshToken refreshToken = RefreshToken.builder()
                .userId(user.getId())
                .tokenHash(TokenUtils.hashToken(rawToken))
                .expiresAt(Instant.now().plusMillis(refreshTokenExpirationMs))
                .build();
        refreshTokenRepository.save(refreshToken);
        return rawToken;
    }

    @Transactional
    public AuthResponse rotateRefreshToken(String rawRefreshToken) {
        RefreshToken storedToken = refreshTokenRepository
                .findByTokenHash(TokenUtils.hashToken(rawRefreshToken))
                .orElseThrow(() -> new UnauthorizedException(Message.Auth.RESET_TOKEN_INVALID));

        if (storedToken.isRevoked()) {
            revokeAllForUser(storedToken.getUserId());
            throw new UnauthorizedException(Message.Auth.REFRESH_TOKEN_REUSE_DETECTED);
        }

        if (storedToken.isExpired()) {
            throw new UnauthorizedException(Message.Auth.REFRESH_TOKEN_EXPIRED);
        }

        storedToken.setRevokedAt(Instant.now());
        refreshTokenRepository.save(storedToken);

        User user = userRepository.findByIdAndDeletedAtIsNull(storedToken.getUserId())
                .orElseThrow(() -> new UnauthorizedException(Message.format(Message.User.NOT_FOUND, storedToken.getUserId())));

        // docs §13.4: re-check status khi verify refresh token
        if (user.getStatus() != UserStatus.ACTIVE) {
            revokeAllForUser(user.getId());
            throw new UnauthorizedException(Message.User.USER_INACTIVE);
        }

        String newRefreshToken = createRefreshToken(user);
        return buildAuthResponse(user, newRefreshToken);
    }

    public AuthResponse buildAuthResponse(User user, String refreshToken) {
        String accessToken = jwtUtils.generateToken(
                user.getId(),
                user.getEmail(),
                user.getEmail(),
                UserMapper.resolveRoleLabel(user));
        return new AuthResponse(
                user.getId(),
                user.getEmail(),
                UserMapper.resolveRoleLabel(user),
                accessToken,
                refreshToken);
    }

    @Transactional
    public void revokeRefreshToken(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            return;
        }
        refreshTokenRepository.findByTokenHash(TokenUtils.hashToken(rawRefreshToken))
                .ifPresent(token -> {
                    token.setRevokedAt(Instant.now());
                    refreshTokenRepository.save(token);
                });
    }

    @Transactional
    public void revokeAllForUser(Long userId) {
        refreshTokenRepository.revokeAllForUser(userId, Instant.now());
    }
}
