package com.dawn.user.service;

import com.dawn.common.exception.wrapper.ConflictException;
import com.dawn.common.exception.wrapper.InvalidRequestException;
import com.dawn.user.controller.request.ForgotPasswordRequest;
import com.dawn.user.controller.request.RegisterRequest;
import com.dawn.user.controller.request.VerifyOtpRequest;
import com.dawn.user.entity.User;
import com.dawn.user.entity.enums.UserStatus;
import com.dawn.user.port.out.EmailPublisherPort;
import com.dawn.user.port.out.EmailVerificationCachePort;
import com.dawn.user.port.out.PasswordResetPort;
import com.dawn.user.port.out.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    UserRepository userRepository;
    @Mock
    PasswordEncoder passwordEncoder;
    @Mock
    RefreshTokenService refreshTokenService;
    @Mock
    EmailPublisherPort emailPublisherPort;
    @Mock
    PasswordResetPort passwordResetPort;
    @Mock
    EmailVerificationCachePort emailVerificationCachePort;

    AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, passwordEncoder, refreshTokenService,
                emailPublisherPort, passwordResetPort, emailVerificationCachePort);
    }

    @Nested
    @DisplayName("Verify OTP — sai không được đốt mã thật")
    class VerifyOtp {

        @Test
        @DisplayName("Sai 1 lần: OTP còn nguyên (chỉ peek, không consume)")
        void wrongOnce_keepsOtp() {
            when(emailVerificationCachePort.peekOtp("a@x.vn")).thenReturn("123456");
            when(emailVerificationCachePort.recordFailedAttempt("a@x.vn")).thenReturn(1L);

            assertThatThrownBy(() -> authService.verify(new VerifyOtpRequest("a@x.vn", "000000")))
                    .isInstanceOf(InvalidRequestException.class);

            verify(emailVerificationCachePort, never()).consumeOtp(anyString());
            verify(emailVerificationCachePort).recordFailedAttempt("a@x.vn");
        }

        @Test
        @DisplayName("Sai 5 lần: OTP bị đốt")
        void wrongFiveTimes_burnsOtp() {
            when(emailVerificationCachePort.peekOtp("a@x.vn")).thenReturn("123456");
            when(emailVerificationCachePort.recordFailedAttempt("a@x.vn")).thenReturn(5L);

            assertThatThrownBy(() -> authService.verify(new VerifyOtpRequest("a@x.vn", "000000")))
                    .isInstanceOf(InvalidRequestException.class);

            verify(emailVerificationCachePort).consumeOtp("a@x.vn");
            verify(emailVerificationCachePort).clearAttempts("a@x.vn");
        }

        @Test
        @DisplayName("Đúng: consume + clear attempts + ACTIVE user")
        void correct_consumesAndActivates() {
            when(emailVerificationCachePort.peekOtp("a@x.vn")).thenReturn("123456");
            User user = User.builder().email("a@x.vn").status(UserStatus.PENDING_VERIFICATION).build();
            when(userRepository.findByEmailAndDeletedAtIsNull("a@x.vn")).thenReturn(Optional.of(user));

            authService.verify(new VerifyOtpRequest("a@x.vn", "123456"));

            verify(emailVerificationCachePort).consumeOtp("a@x.vn");
            verify(emailVerificationCachePort).clearAttempts("a@x.vn");
            assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
        }
    }

    @Nested
    @DisplayName("Forgot password — rate limit + không lộ email")
    class ForgotPassword {

        @Test
        @DisplayName("Email lạ: im lặng, không gửi mail, không báo lỗi")
        void unknownEmail_silent() {
            when(userRepository.findByEmailAndDeletedAtIsNull("ghost@x.vn")).thenReturn(Optional.empty());

            authService.forgotPassword(new ForgotPasswordRequest("ghost@x.vn"));

            verifyNoInteractions(emailPublisherPort, passwordResetPort);
        }

        @Test
        @DisplayName("Đang cooldown: 429 InvalidRequest, không gửi lại mail")
        void cooldown_rejects() {
            User user = User.builder().email("a@x.vn").status(UserStatus.ACTIVE).build();
            when(userRepository.findByEmailAndDeletedAtIsNull("a@x.vn")).thenReturn(Optional.of(user));
            when(passwordResetPort.isOnCooldown("a@x.vn")).thenReturn(true);

            assertThatThrownBy(() -> authService.forgotPassword(new ForgotPasswordRequest("a@x.vn")))
                    .isInstanceOf(InvalidRequestException.class);

            verify(emailPublisherPort, never()).publish(any());
        }
    }

    @Nested
    @DisplayName("Register — đua concurrent")
    class Register {

        @Test
        @DisplayName("Trùng UNIQUE ở DB: 409 thay vì 500")
        void concurrentDuplicate_returns409() {
            when(userRepository.existsByEmailAndDeletedAtIsNull(anyString())).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("hash");
            when(userRepository.save(any())).thenThrow(new DataIntegrityViolationException("dup"));

            assertThatThrownBy(() -> authService.register(
                    new RegisterRequest("A", "dup@x.vn", "password123", "0901")))
                    .isInstanceOf(ConflictException.class);
        }
    }
}
