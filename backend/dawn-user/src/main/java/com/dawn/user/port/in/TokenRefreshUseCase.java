package com.dawn.user.port.in;

import com.dawn.user.controller.response.AuthResponse;

public interface TokenRefreshUseCase {
    AuthResponse refresh(String refreshToken);
}
