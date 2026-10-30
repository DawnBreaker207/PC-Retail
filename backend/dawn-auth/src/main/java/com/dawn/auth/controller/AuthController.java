package com.dawn.auth.controller;

import com.dawn.auth.dto.LoginRequest;
import com.dawn.auth.dto.RefreshRequest;
import com.dawn.auth.dto.RegisterRequest;
import com.dawn.auth.dto.TokenResponse;
import com.dawn.auth.service.AuthService;
import com.dawn.common.utils.JWTUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Auth", description = "Authentication endpoints")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JWTUtils jwtUtils;

    @PostMapping("/register")
    @Operation(summary = "Register a new user account")
    public ResponseEntity<TokenResponse> register(@Valid @RequestBody RegisterRequest req) {
        return ResponseEntity.ok(authService.register(req));
    }

    @PostMapping("/login")
    @Operation(summary = "Login with email and password")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(authService.login(req));
    }

    @PostMapping("/refresh-token")
    @Operation(summary = "Rotate refresh token and obtain new access token")
    public ResponseEntity<TokenResponse> refresh(@Valid @RequestBody RefreshRequest req) {
        return ResponseEntity.ok(authService.refresh(req.refreshToken()));
    }

    @PostMapping("/logout")
    @Operation(summary = "Revoke all tokens and blacklist access token")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        String bearer = extractBearer(request);
        Long userId = bearer != null ? jwtUtils.getUserIdFromToken(bearer) : null;
        if (userId != null) {
            authService.logout(userId, bearer, 900);
        }
        return ResponseEntity.noContent().build();
    }

    private String extractBearer(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
