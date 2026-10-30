package com.dawn.auth.service;

import com.dawn.auth.dto.LoginRequest;
import com.dawn.auth.dto.RegisterRequest;
import com.dawn.auth.dto.TokenResponse;
import com.dawn.auth.entity.UserCredential;
import com.dawn.auth.repository.UserCredentialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserCredentialRepository userCredentialRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    private static final AtomicLong USER_ID_SEQ = new AtomicLong(1000);

    @Transactional
    public TokenResponse register(RegisterRequest req) {
        if (userCredentialRepository.existsByEmail(req.email())) {
            throw new IllegalArgumentException("Email already registered");
        }
        Long userId = Optional.ofNullable(req.userId()).orElseGet(USER_ID_SEQ::incrementAndGet);

        UserCredential cred = UserCredential.builder()
                .userId(userId)
                .email(req.email())
                .passwordHash(passwordEncoder.encode(req.password()))
                .roles(new String[]{"ROLE_USER"})
                .build();
        userCredentialRepository.save(cred);

        return tokenService.issue(cred);
    }

    @Transactional
    public TokenResponse login(LoginRequest req) {
        UserCredential cred = userCredentialRepository.findByEmail(req.email())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        if (cred.isLocked()) throw new IllegalStateException("Account is locked");
        if (!cred.isEnabled()) throw new IllegalStateException("Account is disabled");
        if (!passwordEncoder.matches(req.password(), cred.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        return tokenService.issue(cred);
    }

    @Transactional
    public TokenResponse refresh(String rawRefreshToken) {
        // We need to find the user by refresh token hash before rotating
        String hash = hashToken(rawRefreshToken);
        var stored = tokenService.findByHash(hash)
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

        UserCredential cred = userCredentialRepository.findByUserId(stored.getUserId())
                .orElseThrow(() -> new IllegalStateException("User not found"));

        return tokenService.rotate(rawRefreshToken, cred);
    }

    @Transactional
    public void logout(Long userId, String accessToken, long accessTokenExpiresIn) {
        tokenService.revokeAll(userId);
        if (accessToken != null && !accessToken.isBlank()) {
            tokenService.blacklistAccessToken(accessToken, accessTokenExpiresIn);
        }
    }

    private String hashToken(String raw) {
        try {
            var digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(raw.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return java.util.Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
