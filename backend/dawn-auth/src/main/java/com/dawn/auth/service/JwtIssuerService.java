package com.dawn.auth.service;

import com.dawn.common.utils.JWTUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class JwtIssuerService {

    private final JWTUtils jwtUtils;

    @Value("${app.jwtExpirationsMs}")
    private long jwtExpirationMs;

    public String issue(Long userId, String email, List<String> roles) {
        String role = String.join(",", roles);
        return jwtUtils.generateToken(userId, email, email, role);
    }

    public long expirationMs() {
        return jwtExpirationMs;
    }
}
