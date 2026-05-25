package dev.lgbonillar.regreporting.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security.jwt")
public record JwtProperties(
        String secret,
        String issuer,
        long accessTokenExpirationMinutes,
        long refreshTokenExpirationDays
) {
}
