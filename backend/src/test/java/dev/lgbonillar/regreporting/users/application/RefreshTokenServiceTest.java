package dev.lgbonillar.regreporting.users.application;

import dev.lgbonillar.regreporting.config.JwtProperties;
import dev.lgbonillar.regreporting.shared.ForbiddenOperationException;
import dev.lgbonillar.regreporting.users.domain.AuthSession;
import dev.lgbonillar.regreporting.users.domain.User;
import dev.lgbonillar.regreporting.users.domain.UserStatus;
import dev.lgbonillar.regreporting.users.infrastructure.AuthSessionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private AuthSessionRepository authSessionRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private final JwtProperties jwtProperties =
            new JwtProperties("secret", "regulatory-reporting-api", 15L, 7L);

    @Test
    void createSessionRevokesExistingSessionAndStoresHashedRefreshToken() {
        User user = user();
        AuthSession existingSession = session(user, "old-hash", LocalDateTime.now().plusDays(1L));
        RefreshTokenService refreshTokenService =
                new RefreshTokenService(authSessionRepository, passwordEncoder, jwtProperties);

        when(authSessionRepository.findByUserAndRevokedAtIsNull(user))
                .thenReturn(Optional.of(existingSession));
        when(passwordEncoder.encode(anyString())).thenReturn("new-hash");
        when(authSessionRepository.save(org.mockito.ArgumentMatchers.any(AuthSession.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RefreshTokenService.CreatedRefreshToken result =
                refreshTokenService.createSession(user, "Mozilla", "127.0.0.1");

        assertThat(result.refreshToken()).isNotBlank();
        assertThat(existingSession.getRevokedAt()).isNotNull();
        assertThat(existingSession.getRevokedBy()).isSameAs(user);
        assertThat(existingSession.getRevokeReason()).isEqualTo("Replaced by new login");
        verify(authSessionRepository).saveAndFlush(existingSession);

        ArgumentCaptor<AuthSession> captor = ArgumentCaptor.forClass(AuthSession.class);
        verify(authSessionRepository).save(captor.capture());

        AuthSession savedSession = captor.getValue();
        assertThat(savedSession.getUser()).isSameAs(user);
        assertThat(savedSession.getRefreshTokenHash()).isEqualTo("new-hash");
        assertThat(savedSession.getUserAgent()).isEqualTo("Mozilla");
        assertThat(savedSession.getIpAddress()).isEqualTo("127.0.0.1");
        assertThat(savedSession.getExpiresAt()).isAfter(LocalDateTime.now());
    }

    @Test
    void rotateReplacesRefreshTokenHashAndUpdatesLastUsedAt() {
        User user = user();
        AuthSession session = session(user, "old-hash", LocalDateTime.now().plusDays(1L));
        RefreshTokenService refreshTokenService =
                new RefreshTokenService(authSessionRepository, passwordEncoder, jwtProperties);

        when(authSessionRepository.findAll()).thenReturn(List.of(session));
        when(passwordEncoder.matches("old-refresh-token", "old-hash")).thenReturn(true);
        when(passwordEncoder.encode(anyString())).thenReturn("new-hash");
        when(authSessionRepository.save(session)).thenReturn(session);

        RefreshTokenService.CreatedRefreshToken result =
                refreshTokenService.rotate("old-refresh-token");

        assertThat(result.refreshToken()).isNotBlank();
        assertThat(result.refreshToken()).isNotEqualTo("old-refresh-token");
        assertThat(result.session()).isSameAs(session);
        assertThat(session.getRefreshTokenHash()).isEqualTo("new-hash");
        assertThat(session.getLastUsedAt()).isNotNull();
        assertThat(session.getExpiresAt()).isAfter(LocalDateTime.now());
    }

    @Test
    void rotateFailsWhenRefreshTokenDoesNotMatchAnyActiveSession() {
        User user = user();
        AuthSession session = session(user, "old-hash", LocalDateTime.now().plusDays(1L));
        RefreshTokenService refreshTokenService =
                new RefreshTokenService(authSessionRepository, passwordEncoder, jwtProperties);

        when(authSessionRepository.findAll()).thenReturn(List.of(session));
        when(passwordEncoder.matches("invalid-token", "old-hash")).thenReturn(false);

        assertThatThrownBy(() -> refreshTokenService.rotate("invalid-token"))
                .isInstanceOf(ForbiddenOperationException.class)
                .hasMessage("Invalid refresh token");
    }

    @Test
    void revokeMarksMatchingActiveSessionAsRevoked() {
        User user = user();
        AuthSession session = session(user, "old-hash", LocalDateTime.now().plusDays(1L));
        RefreshTokenService refreshTokenService =
                new RefreshTokenService(authSessionRepository, passwordEncoder, jwtProperties);

        when(authSessionRepository.findAll()).thenReturn(List.of(session));
        when(passwordEncoder.matches("refresh-token", "old-hash")).thenReturn(true);

        refreshTokenService.revoke("refresh-token", user, "User logout");

        assertThat(session.getRevokedAt()).isNotNull();
        assertThat(session.getRevokedBy()).isSameAs(user);
        assertThat(session.getRevokeReason()).isEqualTo("User logout");
        verify(authSessionRepository).save(session);
    }

    private AuthSession session(User user, String refreshTokenHash, LocalDateTime expiresAt) {
        return new AuthSession(
                user,
                refreshTokenHash,
                "Mozilla",
                "127.0.0.1",
                expiresAt
        );
    }

    private User user() {
        User user = new User(
                "analyst01",
                "analyst01@example.com",
                "Analyst 01",
                "password-hash",
                false,
                UserStatus.ACTIVE
        );
        user.setId(UUID.randomUUID());
        return user;
    }

}
