package dev.lgbonillar.regreporting.config;

import dev.lgbonillar.regreporting.users.infrastructure.AuthSessionRepository;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ActiveSessionJwtValidator implements OAuth2TokenValidator<Jwt> {

    private static final OAuth2Error INVALID_SESSION =
            new OAuth2Error("invalid_session", "Session is not active", null);

    private final AuthSessionRepository authSessionRepository;

    public ActiveSessionJwtValidator(AuthSessionRepository authSessionRepository) {
        this.authSessionRepository = authSessionRepository;
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt token) {
        String sessionId = token.getClaimAsString("sessionId");

        if (sessionId == null || sessionId.isBlank()) {
            return OAuth2TokenValidatorResult.failure(INVALID_SESSION);
        }

        boolean activeSession = authSessionRepository
                .findByIdAndRevokedAtIsNull(UUID.fromString(sessionId))
                .filter(session -> session.getExpiresAt().isAfter(java.time.LocalDateTime.now()))
                .isPresent();

        if (!activeSession) {
            return OAuth2TokenValidatorResult.failure(INVALID_SESSION);
        }

        return OAuth2TokenValidatorResult.success();
    }

}
