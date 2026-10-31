package com.dawn.user.service;

import com.dawn.common.exception.wrapper.InvalidRequestException;
import com.dawn.common.exception.wrapper.ConflictException;
import com.dawn.common.exception.wrapper.ResourceNotFoundException;
import com.dawn.common.exception.wrapper.UnauthorizedException;
import com.dawn.common.message.Message;
import com.dawn.user.controller.request.*;
import com.dawn.user.controller.response.AuthResponse;
import com.dawn.user.controller.response.RegisterInitResponse;
import com.dawn.user.dto.EmailMessage;
import com.dawn.user.entity.User;
import com.dawn.user.entity.enums.SystemRole;
import com.dawn.user.entity.enums.UserStatus;
import com.dawn.user.port.in.*;
import com.dawn.user.port.out.EmailPublisherPort;
import com.dawn.user.port.out.EmailVerificationCachePort;
import com.dawn.user.port.out.PasswordResetPort;
import com.dawn.user.port.out.UserRepository;
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


    @Override
    @Transactional
    public RegisterInitResponse register(RegisterRequest request) {
        if (userRepository.existsByEmailAndDeletedAtIsNull(request.email())) {
            throw new ConflictException(Message.User.EMAIL_ALREADY_USED);
        }

        User user = User.builder()
                .email(request.email().toLowerCase())
                .passwordHash(passwordEncoder.encode(request.password()))
                .fullName(request.fullName())
                .phone(request.phone())
                .systemRole(SystemRole.CUSTOMER)
                .status(UserStatus.PENDING_VERIFICATION)
                .build();

        try {
            userRepository.save(user);
        } catch (org.springframework.dao.DataIntegrityViolationException dup) {
            // concurrent check-then-insert race: return 409 instead of 500
            throw new ConflictException(Message.User.EMAIL_ALREADY_USED);
        }


        sendOtp(user.getEmail(), user.getFullName());


        return new RegisterInitResponse(user.getId(), user.getEmail(), "Vui lòng kiểm tra email để lấy mã xác thực");
    }

    // peek OTP before consuming; burn the code after 5 failed attempts
    @Override
    @Transactional
    public AuthResponse verify(VerifyOtpRequest request) {
        String email = request.email().toLowerCase();

        String storedOtp = emailVerificationCachePort.peekOtp(email);
        if (storedOtp == null || !storedOtp.equals(request.otpCode())) {
            long fails = emailVerificationCachePort.recordFailedAttempt(email);
            if (fails >= 5) {
                emailVerificationCachePort.consumeOtp(email);
                emailVerificationCachePort.clearAttempts(email);
            }
            throw new InvalidRequestException(Message.Auth.TOKEN_INVALID_OR_EXPIRED);
        }
        emailVerificationCachePort.consumeOtp(email);
        emailVerificationCachePort.clearAttempts(email);
        User user = userRepository
                .findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new ResourceNotFoundException(Message.format(Message.User.USERNAME_NOT_FOUND, email)));

        if (user.getStatus() != UserStatus.PENDING_VERIFICATION) {
            throw new InvalidRequestException(Message.User.USER_ALREADY_VERIFICATION);
        }

        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);

        emailPublisherPort.publish(EmailMessage.welcome(user.getEmail(), user.getFullName()));

        String refreshToken = refreshTokenService.createRefreshToken(user);

        return refreshTokenService.buildAuthResponse(user, refreshToken);
    }

    @Override
    public void resend(String email) {
        String normalizedEmail = email.toLowerCase();

        if (emailVerificationCachePort.isOnCooldown(normalizedEmail)) {
            throw new InvalidRequestException(Message.Auth.OTP_RESEND_COOLDOWN);
        }

        User user = userRepository.findByEmailAndDeletedAtIsNull(normalizedEmail)
                .orElseThrow(() -> new ResourceNotFoundException(Message.format(Message.User.NOT_FOUND, normalizedEmail)));
        if (user.getStatus() != UserStatus.PENDING_VERIFICATION) {
            throw new InvalidRequestException(Message.User.USER_ALREADY_VERIFICATION);
        }

        sendOtp(user.getEmail(), user.getFullName());
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmailAndDeletedAtIsNull(request.email().toLowerCase())
                .orElseThrow(() -> new UnauthorizedException(Message.Auth.INVALID_CREDENTIALS));

        if (user.getStatus() == UserStatus.PENDING_VERIFICATION) {
            throw new UnauthorizedException(Message.Auth.ACCOUNT_NOT_VERIFIED);
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new UnauthorizedException(Message.Auth.ACCOUNT_NOT_ACTIVE);
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new UnauthorizedException(Message.Auth.INVALID_CREDENTIALS);
        }

        String refreshToken = refreshTokenService.createRefreshToken(user);
        return refreshTokenService.buildAuthResponse(user, refreshToken);
    }


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

    // does not reveal whether the email exists; rate-limited to 1 req/60s
    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        String email = request.email().toLowerCase();
        var userOpt = userRepository.findByEmailAndDeletedAtIsNull(email);
        if (userOpt.isEmpty()) return;
        if (passwordResetPort.isOnCooldown(email)) {
            throw new InvalidRequestException(Message.Auth.OTP_RESEND_COOLDOWN);
        }
        var user = userOpt.get();
        String rawToken = TokenUtils.generateResetToken();
        passwordResetPort.storeResetToken(TokenUtils.hashToken(rawToken), user.getId());
        passwordResetPort.storeCooldown(email);
        emailPublisherPort.publish(EmailMessage.passwordReset(
                user.getEmail(), user.getFullName(), rawToken));
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        Long userId = passwordResetPort.consumeResetToken(TokenUtils.hashToken(request.token()));
        if (userId == null) {
            throw new InvalidRequestException(Message.Auth.RESET_TOKEN_INVALID);
        }

        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new ResourceNotFoundException(Message.format(Message.User.NOT_FOUND, userId)));

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        refreshTokenService.revokeAllForUser(userId);
    }


    private void sendOtp(String email, String fullName) {
        String otp = TokenUtils.generateOtp();
        emailVerificationCachePort.storeOtp(email, otp);
        emailVerificationCachePort.storeCooldown(email);
        emailPublisherPort.publish(EmailMessage.otpVerification(email, fullName, otp));
    }
}
