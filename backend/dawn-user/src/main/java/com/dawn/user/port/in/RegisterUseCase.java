package com.dawn.user.port.in;

import com.dawn.user.controller.request.RegisterRequest;
import com.dawn.user.controller.response.AuthResponse;
import com.dawn.user.controller.response.RegisterInitResponse;

public interface RegisterUseCase {
    RegisterInitResponse register(RegisterRequest request);
}
