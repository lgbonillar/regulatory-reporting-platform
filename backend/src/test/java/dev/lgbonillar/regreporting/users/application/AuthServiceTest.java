package dev.lgbonillar.regreporting.users.application;

import dev.lgbonillar.regreporting.config.JwtProperties;
import dev.lgbonillar.regreporting.shared.ForbiddenOperationException;
import dev.lgbonillar.regreporting.users.domain.AuthSession;
import dev.lgbonillar.regreporting.users.domain.User;
import dev.lgbonillar.regreporting.users.domain.UserStatus;
import dev.lgbonillar.regreporting.users.dto.AuthResponse;
import dev.lgbonillar.regreporting.users.infrastructure.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenService jwtTokenService;

    @Mock
    private RefreshTokenService refreshTokenService;

    private final JwtProperties jwtProperties =
            new JwtProperties("secret", "regulatory-reporting-api", 15L, 7L);

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                userRepository,
                passwordEncoder,
                jwtTokenService,
                refreshTokenService,
                jwtProperties
        );
    }

    @Test
    void loginReturnsAccessAndRefreshTokensWhenCredentialsAreValid() {
        User user = user(UserStatus.ACTIVE);
        UUID sessionId = UUID.randomUUID();
        AuthSession session = session(user, sessionId);
        RefreshTokenService.CreatedRefreshToken createdRefreshToken =
                new RefreshTokenService.CreatedRefreshToken("refresh-token", session);

        when(userRepository.findByUsername("analyst01")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", "password-hash")).thenReturn(true);
        when(refreshTokenService.createSession(user, "Mozilla", "127.0.0.1"))
                .thenReturn(createdRefreshToken);
        when(jwtTokenService.createAccessToken(user, sessionId)).thenReturn("access-token");

        AuthResponse result = authService.login("analyst01", "password", "Mozilla", "127.0.0.1");

        assertThat(result.accessToken()).isEqualTo("access-token");
        assertThat(result.refreshToken()).isEqualTo("refresh-token");
        assertThat(result.tokenType()).isEqualTo("Bearer");
        assertThat(result.expiresInSeconds()).isEqualTo(900L);
    }

    @Test
    void loginFailsWhenUserDoesNotExist() {
        when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login("missing", "password", "Mozilla", "127.0.0.1"))
                .isInstanceOf(ForbiddenOperationException.class)
                .hasMessage("Invalid credentials");

        verifyNoInteractions(passwordEncoder, refreshTokenService, jwtTokenService);
    }

    @Test
    void loginFailsWhenPasswordIsInvalid() {
        User user = user(UserStatus.ACTIVE);

        when(userRepository.findByUsername("analyst01")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "password-hash")).thenReturn(false);

        assertThatThrownBy(() -> authService.login("analyst01", "wrong-password", "Mozilla", "127.0.0.1"))
                .isInstanceOf(ForbiddenOperationException.class)
                .hasMessage("Invalid credentials");

        verifyNoInteractions(refreshTokenService, jwtTokenService);
    }

    @Test
    void loginFailsWhenUserIsNotActive() {
        User user = user(UserStatus.INACTIVE);

        when(userRepository.findByUsername("analyst01")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.login("analyst01", "password", "Mozilla", "127.0.0.1"))
                .isInstanceOf(ForbiddenOperationException.class)
                .hasMessage("User is not active");

        verifyNoInteractions(passwordEncoder, refreshTokenService, jwtTokenService);
    }

    @Test
    void refreshRotatesRefreshTokenAndReturnsNewAccessToken() {
        User user = user(UserStatus.ACTIVE);
        UUID sessionId = UUID.randomUUID();
        AuthSession session = session(user, sessionId);
        RefreshTokenService.CreatedRefreshToken createdRefreshToken =
                new RefreshTokenService.CreatedRefreshToken("new-refresh-token", session);

        when(refreshTokenService.rotate("old-refresh-token")).thenReturn(createdRefreshToken);
        when(jwtTokenService.createAccessToken(user, sessionId)).thenReturn("new-access-token");

        AuthResponse result = authService.refresh("old-refresh-token");

        assertThat(result.accessToken()).isEqualTo("new-access-token");
        assertThat(result.refreshToken()).isEqualTo("new-refresh-token");
        assertThat(result.expiresInSeconds()).isEqualTo(900L);
    }

    @Test
    void logoutRevokesRefreshToken() {
        User user = user(UserStatus.ACTIVE);

        authService.logout("refresh-token", user);

        verify(refreshTokenService).revoke("refresh-token", user, "User logout");
    }

    private User user(UserStatus status) {
        User user = new User(
                "analyst01",
                "analyst01@example.com",
                "Analyst 01",
                "password-hash",
                false,
                status
        );
        user.setId(UUID.randomUUID());
        return user;
    }

    private AuthSession session(User user, UUID sessionId) {
        AuthSession session = new AuthSession(
                user,
                "refresh-token-hash",
                "Mozilla",
                "127.0.0.1",
                LocalDateTime.now().plusDays(1L)
        );
        ReflectionTestUtils.setField(session, "id", sessionId);
        return session;
    }

}
