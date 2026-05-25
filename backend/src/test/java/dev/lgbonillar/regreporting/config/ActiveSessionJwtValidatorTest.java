package dev.lgbonillar.regreporting.config;

import dev.lgbonillar.regreporting.users.domain.AuthSession;
import dev.lgbonillar.regreporting.users.domain.User;
import dev.lgbonillar.regreporting.users.domain.UserStatus;
import dev.lgbonillar.regreporting.users.infrastructure.AuthSessionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ActiveSessionJwtValidatorTest {

    @Mock
    private AuthSessionRepository authSessionRepository;

    @Test
    void validateSucceedsWhenSessionIsActive() {
        UUID sessionId = UUID.randomUUID();
        AuthSession session = session(LocalDateTime.now().plusMinutes(15L));
        ActiveSessionJwtValidator validator = new ActiveSessionJwtValidator(authSessionRepository);

        when(authSessionRepository.findByIdAndRevokedAtIsNull(sessionId))
                .thenReturn(Optional.of(session));

        OAuth2TokenValidatorResult result = validator.validate(jwt(Map.of(
                "sessionId", sessionId.toString()
        )));

        assertThat(result.hasErrors()).isFalse();
    }

    @Test
    void validateFailsWhenSessionIdIsMissing() {
        ActiveSessionJwtValidator validator = new ActiveSessionJwtValidator(authSessionRepository);

        OAuth2TokenValidatorResult result = validator.validate(jwt(Map.of()));

        assertThat(result.hasErrors()).isTrue();
        assertThat(result.getErrors()).extracting("errorCode")
                .contains("invalid_session");
    }

    @Test
    void validateFailsWhenSessionIsRevoked() {
        UUID sessionId = UUID.randomUUID();
        ActiveSessionJwtValidator validator = new ActiveSessionJwtValidator(authSessionRepository);

        when(authSessionRepository.findByIdAndRevokedAtIsNull(sessionId))
                .thenReturn(Optional.empty());

        OAuth2TokenValidatorResult result = validator.validate(jwt(Map.of(
                "sessionId", sessionId.toString()
        )));

        assertThat(result.hasErrors()).isTrue();
        assertThat(result.getErrors()).extracting("errorCode")
                .contains("invalid_session");
    }

    @Test
    void validateFailsWhenSessionIsExpired() {
        UUID sessionId = UUID.randomUUID();
        AuthSession session = session(LocalDateTime.now().minusMinutes(1L));
        ActiveSessionJwtValidator validator = new ActiveSessionJwtValidator(authSessionRepository);

        when(authSessionRepository.findByIdAndRevokedAtIsNull(sessionId))
                .thenReturn(Optional.of(session));

        OAuth2TokenValidatorResult result = validator.validate(jwt(Map.of(
                "sessionId", sessionId.toString()
        )));

        assertThat(result.hasErrors()).isTrue();
        assertThat(result.getErrors()).extracting("errorCode")
                .contains("invalid_session");
    }

    private Jwt jwt(Map<String, Object> claims) {
        Map<String, Object> jwtClaims = claims.isEmpty()
                ? Map.of("sub", "analyst01")
                : claims;

        return new Jwt(
                "token",
                Instant.now(),
                Instant.now().plusSeconds(60L),
                Map.of("alg", "HS256"),
                jwtClaims
        );
    }

    private AuthSession session(LocalDateTime expiresAt) {
        return new AuthSession(
                user(),
                "refresh-token-hash",
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
