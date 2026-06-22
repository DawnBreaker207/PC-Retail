package com.dawn.user.controller.response;

public record AuthResponse(
        Long userId,
        String email,
        String role,
        String accessToken,
        String refreshToken) {
}
