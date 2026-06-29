package com.dawn.user.controller;

import com.dawn.common.response.ResponseObject;
import com.dawn.user.controller.request.*;
import com.dawn.user.controller.response.AuthResponse;
import com.dawn.user.controller.response.RegisterInitResponse;
import com.dawn.user.port.in.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final RegisterUseCase registerUseCase;
    private final LoginUseCase loginUseCase;
    private final TokenRefreshUseCase tokenRefreshUseCase;
    private final LogoutUseCase logoutUseCase;
    private final PasswordResetUseCase passwordResetUseCase;
    private final ResendRegistrationOtpUseCase resendOtpUseCase;
    private final VerifyRegistrationOtpUseCase verifyOtpUseCase;

    @PostMapping("/register")
    public ResponseObject<RegisterInitResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseObject.created(registerUseCase.register(request));
    }

    @PostMapping("/login")
    public ResponseObject<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseObject.success(loginUseCase.login(request));
    }

    @PostMapping("/refresh")
    public ResponseObject<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseObject.success(tokenRefreshUseCase.refresh(request.refreshToken()));
    }

    @PostMapping("/logout")
    public ResponseObject<Void> logout(@Valid @RequestBody RefreshTokenRequest request) {
        logoutUseCase.logout(request.refreshToken());
        return ResponseObject.success(null, "Logged out successfully");
    }

    @PostMapping("/forgot-password")
    public ResponseObject<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetUseCase.forgotPassword(request);
        return ResponseObject.success(null, "If the email exists, a reset link has been sent");
    }

    @PostMapping("/reset-password")
    public ResponseObject<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetUseCase.resetPassword(request);
        return ResponseObject.success(null, "Password reset successfully");
    }

    @PostMapping("/verify-otp")
    public ResponseObject<AuthResponse> verifyOtp(@Valid @RequestBody VerifyOtpRequest req) {
        return ResponseObject.success(verifyOtpUseCase.verify(req));
    }

    @PostMapping("/resend-otp")
    public ResponseObject<Void> resendOtp(@RequestParam String email) {
        resendOtpUseCase.resend(email);
        return ResponseObject.success(null, "Resend OTP successfully");
    }
}
