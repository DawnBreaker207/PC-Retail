package com.dawn.user.controller;

import com.dawn.common.response.ResponseObject;
import com.dawn.common.utils.SecurityUtils;
import com.dawn.user.controller.request.*;
import com.dawn.user.controller.response.AuthResponse;
import com.dawn.user.controller.response.RegisterInitResponse;
import com.dawn.user.port.in.*;
import com.dawn.user.service.PhoneOtpService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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
    private final PhoneOtpService phoneOtpService;

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

    // ---- Guest phone OTP (docs 13.5 risk-gate, public) ----

    @GetMapping("/otp/challenge")
    public ResponseObject<Map<String, String>> otpChallenge() {
        return ResponseObject.success(phoneOtpService.challenge());
    }

    @PostMapping("/otp/request")
    public ResponseObject<Map<String, Object>> requestPhoneOtp(
            @Valid @RequestBody PhoneOtpRequest req, HttpServletRequest http) {
        return ResponseObject.success(phoneOtpService.request(
                req.phone(), req.challengeId(), req.challengeAnswer(),
                SecurityUtils.getCurrentClientIp(http)));
    }

    @PostMapping("/otp/verify")
    public ResponseObject<Map<String, Boolean>> verifyPhoneOtp(
            @Valid @RequestBody PhoneOtpVerifyRequest req) {
        return ResponseObject.success(Map.of("verified", phoneOtpService.verify(req.phone(), req.code())));
    }

    @GetMapping("/otp/debug/{phone}")
    public ResponseObject<Map<String, String>> debugPhoneOtp(@PathVariable String phone) {
        String code = phoneOtpService.debugPeek(phone);
        if (code == null) {
            throw new com.dawn.common.exception.wrapper.ResourceNotFoundException("No OTP for phone");
        }
        return ResponseObject.success(Map.of("code", code));
    }
}
