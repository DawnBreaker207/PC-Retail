package com.dawn.user.service;

import com.dawn.common.exception.wrapper.UnauthorizedException;
import com.dawn.common.utils.JWTUtils;
import com.dawn.user.entity.RefreshToken;
import com.dawn.user.entity.User;
import com.dawn.user.entity.enums.UserStatus;
import com.dawn.user.infra.persistence.JpaUserRepository;
import com.dawn.user.infra.persistence.RefreshTokenRepository;
import com.dawn.user.util.TokenUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    RefreshTokenRepository refreshTokenRepository;
    @Mock
    JpaUserRepository userRepository;
    @Mock
    JWTUtils jwtUtils;

    RefreshTokenService service;

    @BeforeEach
    void setUp() {
        service = new RefreshTokenService(refreshTokenRepository, userRepository, jwtUtils);
    }

    private RefreshToken token(Long userId, Instant expiresAt, Instant revokedAt) {
        return RefreshToken.builder()
                .userId(userId).tokenHash("hash-1")
                .expiresAt(expiresAt).revokedAt(revokedAt).build();
    }

    private User activeUser() {
        User u = User.builder().email("a@x.vn").status(UserStatus.ACTIVE).build();
        u.setId(7L);
        return u;
    }

    @Nested
    @DisplayName("rotateRefreshToken")
    class Rotate {

        @Test
        @DisplayName("Token lạ: 401")
        void unknownToken_unauthorized() {
            when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.rotateRefreshToken("raw"))
                    .isInstanceOf(UnauthorizedException.class);
        }

        @Test
        @DisplayName("Reuse token đã revoke: revoke toàn bộ + 401")
        void reuse_revokesAllAndThrows() {
            RefreshToken revoked = token(7L, Instant.now().plusSeconds(600), Instant.now());
            when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(revoked));

            assertThatThrownBy(() -> service.rotateRefreshToken("raw"))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessageContaining("reuse");

            verify(refreshTokenRepository).revokeAllForUser(eq(7L), any(Instant.class));
        }

        @Test
        @DisplayName("Hết hạn: 401, không revoke-all")
        void expired_unauthorized() {
            RefreshToken expired = token(7L, Instant.now().minusSeconds(10), null);
            when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(expired));

            assertThatThrownBy(() -> service.rotateRefreshToken("raw"))
                    .isInstanceOf(UnauthorizedException.class);

            verify(refreshTokenRepository, never()).revokeAllForUser(anyLong(), any());
        }

        @Test
        @DisplayName("User bị BAN: revoke-all + 401 dù token còn hạn")
        void bannedUser_revoked() {
            RefreshToken valid = token(7L, Instant.now().plusSeconds(600), null);
            when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(valid));
            User banned = User.builder().email("a@x.vn").status(UserStatus.BANNED).build();
            banned.setId(7L);
            when(userRepository.findByIdAndDeletedAtIsNull(7L)).thenReturn(Optional.of(banned));

            assertThatThrownBy(() -> service.rotateRefreshToken("raw"))
                    .isInstanceOf(UnauthorizedException.class);

            verify(refreshTokenRepository).revokeAllForUser(eq(7L), any(Instant.class));
        }

        @Test
        @DisplayName("Happy path: xoay vòng, token cũ bị revoke, trả session mới")
        void happyPath_rotates() {
            RefreshToken valid = token(7L, Instant.now().plusSeconds(600), null);
            when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(valid));
            when(userRepository.findByIdAndDeletedAtIsNull(7L)).thenReturn(Optional.of(activeUser()));
            when(jwtUtils.generateToken(anyLong(), anyString(), anyString(), anyString()))
                    .thenReturn("access");

            var res = service.rotateRefreshToken("raw");

            assertThat(valid.getRevokedAt()).isNotNull();
            assertThat(res.accessToken()).isEqualTo("access");
            assertThat(res.refreshToken()).isNotBlank();
            verify(refreshTokenRepository).save(valid);
        }
    }

    @Nested
    @DisplayName("logout / revoke")
    class Logout {
        @Test
        @DisplayName("Logout token null/rỗng: no-op, không nổ")
        void blankToken_noop() {
            service.revokeRefreshToken(null);
            service.revokeRefreshToken("  ");

            verifyNoInteractions(refreshTokenRepository);
        }

        @Test
        @DisplayName("createRefreshToken hash SHA-256 rồi mới lưu")
        void create_hashesBeforeSave() {
            User user = activeUser();
            String raw = service.createRefreshToken(user);

            assertThat(raw).isNotBlank();
            verify(refreshTokenRepository).save(argThat(t ->
                    t.getTokenHash().equals(TokenUtils.hashToken(raw))
                            && !t.getTokenHash().equals(raw)));
        }
    }
}
