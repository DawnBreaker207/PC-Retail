package com.dawn.user.controller.response;

public record RegisterInitResponse(
        Long userId,
        String email,
        String message) {
}
