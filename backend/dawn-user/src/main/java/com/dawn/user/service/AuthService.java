package com.dawn.user.service;

import com.dawn.common.exception.wrapper.InvalidRequestException;
import com.dawn.common.exception.wrapper.ResourceAlreadyExistedException;
import com.dawn.common.exception.wrapper.ResourceNotFoundException;
import com.dawn.common.exception.wrapper.UnauthorizedException;
import com.dawn.user.controller.request.*;
import com.dawn.user.controller.response.AuthResponse;
import com.dawn.user.controller.response.RegisterInitResponse;
import com.dawn.user.dto.EmailMessage;
import com.dawn.user.entity.User;
import com.dawn.user.entity.enums.SystemRole;
import com.dawn.user.entity.enums.UserStatus;
import com.dawn.user.infra.persistence.UserRepository;
import com.dawn.user.port.in.*;
import com.dawn.user.port.out.EmailPublisherPort;
import com.dawn.user.port.out.EmailVerificationCachePort;
import com.dawn.user.port.out.PasswordResetPort;
import com.dawn.user.util.TokenUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService implements RegisterUseCase, LoginUseCase, TokenRefreshUseCase,
        LogoutUseCase, PasswordResetUseCase, VerifyRegistrationOtpUseCase, ResendRegistrationOtpUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final EmailPublisherPort emailPublisherPort;
    private final PasswordResetPort passwordResetPort;
    private final EmailVerificationCachePort emailVerificationCachePort;


    // REGISTER
    @Override
    @Transactional
    public RegisterInitResponse register(RegisterRequest request) {
        if (userRepository.existsByEmailAndDeletedAtIsNull(request.email())) {
            throw new ResourceAlreadyExistedException("Email already exists");
        }

        User user = User.builder()
                .email(request.email().toLowerCase())
                .passwordHash(passwordEncoder.encode(request.password()))
                .fullName(request.fullName())
                .phone(request.phone())
                .systemRole(SystemRole.CUSTOMER)
                .status(UserStatus.PENDING_VERIFICATION)
                .build();

        userRepository.save(user);


        sendOtp(user.getEmail(), user.getFullName());


        return new RegisterInitResponse(user.getId(), user.getEmail(), "Vui lòng kiểm tra email để lấy mã xác thực");
    }

    // Verify OTP
    @Override
    @Transactional
    public AuthResponse verify(VerifyOtpRequest request) {
        String email = request.email().toLowerCase();

        String storeOtp = emailVerificationCachePort.consumerOtp(email);
        if (storeOtp == null || !storeOtp.equals(request.otpCode())) {
            throw new InvalidRequestException("Mã xác thực không hợp lệ hoặc đã hết hạn");
        }
        User user = userRepository
                .findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getStatus() != UserStatus.PENDING_VERIFICATION) {
            throw new InvalidRequestException("Tài khoản đã được xác thực trước đó");
        }

        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);

        emailPublisherPort.publish(EmailMessage.welcome(user.getEmail(), user.getFullName()));

        String refreshToken = refreshTokenService.createRefreshToken(user);

        return refreshTokenService.buildAuthResponse(user, refreshToken);
    }

    // RESEND OTP
    @Override
    public void resend(String email) {
        String normalizedEmail = email.toLowerCase();

        if (emailVerificationCachePort.isOnCooldown(normalizedEmail)) {
            throw new InvalidRequestException("Vui lòng chờ 60 giây trước khi gửi lại mã");
        }

        User user = userRepository.findByEmailAndDeletedAtIsNull(normalizedEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (user.getStatus() != UserStatus.PENDING_VERIFICATION) {
            throw new InvalidRequestException("Tài khoản đã được xác thực");
        }

        sendOtp(user.getEmail(), user.getFullName());
    }

    // LOGIN
    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmailAndDeletedAtIsNull(request.email().toLowerCase())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (user.getStatus() == UserStatus.PENDING_VERIFICATION) {
            throw new UnauthorizedException("Tài khoản chưa được xác thực email");
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new UnauthorizedException("Account is not active");
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        String refreshToken = refreshTokenService.createRefreshToken(user);
        return refreshTokenService.buildAuthResponse(user, refreshToken);
    }


    // REFRESH / LOGOUT
    @Override
    @Transactional
    public AuthResponse refresh(String refreshToken) {
        return refreshTokenService.rotateRefreshToken(refreshToken);
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
        refreshTokenService.revokeRefreshToken(refreshToken);
    }

    // FORGOT PASSWORD
    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        userRepository.findByEmailAndDeletedAtIsNull(request.email().toLowerCase())
                .ifPresent(user -> {
                    String rawToken = TokenUtils.generateResetToken();
                    passwordResetPort.storeResetToken(TokenUtils.hashToken(rawToken), user.getId());
                    emailPublisherPort.publish(EmailMessage.passwordReset(
                            user.getEmail(), user.getFullName(), rawToken));
                });
    }

    // RESET PASSWORD
    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        Long userId = passwordResetPort.consumeResetToken(TokenUtils.hashToken(request.token()));
        if (userId == null) {
            throw new InvalidRequestException("Invalid or expired reset token");
        }

        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        refreshTokenService.revokeAllForUser(userId);
    }


    // Private Helper
    private void sendOtp(String email, String fullName) {
        String otp = TokenUtils.generateOtp();
        emailVerificationCachePort.storeOtp(email, otp);
        emailVerificationCachePort.storeCooldown(email);
        emailPublisherPort.publish(EmailMessage.otpVerification(email, fullName, otp));
    }
}
