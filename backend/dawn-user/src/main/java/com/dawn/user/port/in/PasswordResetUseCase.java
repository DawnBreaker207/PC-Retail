package com.dawn.user.port.in;

import com.dawn.user.controller.request.ForgotPasswordRequest;
import com.dawn.user.controller.request.ResetPasswordRequest;

public interface PasswordResetUseCase {
    void forgotPassword(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);
}
