package com.dawn.user.port.in;

import com.dawn.user.controller.request.VerifyOtpRequest;
import com.dawn.user.controller.response.AuthResponse;

public interface VerifyRegistrationOtpUseCase {
    AuthResponse verify(VerifyOtpRequest request);
}
