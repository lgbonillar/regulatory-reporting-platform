package com.mrcrafterman.regreporting.users.application;

import com.mrcrafterman.regreporting.config.JwtProperties;
import com.mrcrafterman.regreporting.users.domain.User;
import com.mrcrafterman.regreporting.users.domain.UserRole;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.UUID;

@Service
public class JwtTokenService {

    private final JwtEncoder jwtEncoder;
    private final JwtProperties jwtProperties;

    public JwtTokenService(
            JwtEncoder jwtEncoder,
            JwtProperties jwtProperties
    ) {
        this.jwtEncoder = jwtEncoder;
        this.jwtProperties = jwtProperties;
    }

    public String createAccessToken(User user, UUID sessionId) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(
                jwtProperties.accessTokenExpirationMinutes(),
                ChronoUnit.MINUTES
        );

        UserRole role = resolvePrimaryRole(user);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(jwtProperties.issuer())
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .subject(user.getId().toString())
                .claim("userId", user.getId().toString())
                .claim("username", user.getUsername())
                .claim("role", role.name())
                .claim("sessionId", sessionId.toString())
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();

        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims))
                .getTokenValue();
    }

    private UserRole resolvePrimaryRole(User user) {
        return user.getRoles()
                .stream()
                .map(role -> UserRole.valueOf(role.getCode()))
                .min(Comparator.comparingInt(this::rolePriority))
                .orElseThrow(() -> new IllegalStateException("User has no assigned role"));
    }

    private int rolePriority(UserRole role) {
        return switch (role) {
            case ROOT -> 0;
            case ADMINISTRATOR -> 1;
            case ANALYST -> 2;
            case AUDITOR -> 3;
        };
    }

}
