package com.mrcrafterman.regreporting.users.application;

import com.mrcrafterman.regreporting.config.JwtProperties;
import com.mrcrafterman.regreporting.shared.ForbiddenOperationException;
import com.mrcrafterman.regreporting.users.domain.AuthSession;
import com.mrcrafterman.regreporting.users.domain.User;
import com.mrcrafterman.regreporting.users.infrastructure.AuthSessionRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
public class RefreshTokenService {

    private static final int REFRESH_TOKEN_BYTE_SIZE = 64;

    private final AuthSessionRepository authSessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProperties jwtProperties;
    private final SecureRandom secureRandom = new SecureRandom();

    public RefreshTokenService(
            AuthSessionRepository authSessionRepository,
            PasswordEncoder passwordEncoder,
            JwtProperties jwtProperties
    ) {
        this.authSessionRepository = authSessionRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtProperties = jwtProperties;
    }

    public CreatedRefreshToken createSession(
            User user,
            String userAgent,
            String ipAddress
    ) {
        authSessionRepository.findByUserAndRevokedAtIsNull(user)
                .ifPresent(session -> session.revoke(user, "Replaced by new login"));

        String refreshToken = generateRefreshToken();
        AuthSession session = new AuthSession(
                user,
                passwordEncoder.encode(refreshToken),
                userAgent,
                ipAddress,
                LocalDateTime.now().plusDays(jwtProperties.refreshTokenExpirationDays())
        );

        AuthSession savedSession = authSessionRepository.save(session);

        return new CreatedRefreshToken(refreshToken, savedSession);
    }

    public CreatedRefreshToken rotate(String refreshToken) {
        AuthSession session = authSessionRepository.findAll()
                .stream()
                .filter(AuthSession::isActive)
                .filter(candidate -> passwordEncoder.matches(refreshToken, candidate.getRefreshTokenHash()))
                .findFirst()
                .orElseThrow(() -> new ForbiddenOperationException("Invalid refresh token"));

        String newRefreshToken = generateRefreshToken();

        session.rotateRefreshToken(
                passwordEncoder.encode(newRefreshToken),
                LocalDateTime.now().plusDays(jwtProperties.refreshTokenExpirationDays())
        );

        AuthSession savedSession = authSessionRepository.save(session);

        return new CreatedRefreshToken(newRefreshToken, savedSession);
    }

    public void revoke(String refreshToken, User revokedBy, String reason) {
        authSessionRepository.findAll()
                .stream()
                .filter(AuthSession::isActive)
                .filter(candidate -> passwordEncoder.matches(refreshToken, candidate.getRefreshTokenHash()))
                .findFirst()
                .ifPresent(session -> {
                    session.revoke(revokedBy, reason);
                    authSessionRepository.save(session);
                });
    }

    private String generateRefreshToken() {
        byte[] bytes = new byte[REFRESH_TOKEN_BYTE_SIZE];
        secureRandom.nextBytes(bytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

    public record CreatedRefreshToken(
            String refreshToken,
            AuthSession session
    ) {
    }

}
