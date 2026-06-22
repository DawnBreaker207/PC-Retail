package com.dawn.user.port.in;

import com.dawn.user.controller.request.LoginRequest;
import com.dawn.user.controller.response.AuthResponse;

public interface LoginUseCase {
    AuthResponse login(LoginRequest request);
}
