package com.dawn.user.controller.response;

public record UserResponse(
        Long userId,
        String fullName,
        String email,
        String phone,
        String role) {
}
