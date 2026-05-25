package dev.lgbonillar.regreporting.users.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "JWT authentication response.")
public record AuthResponse(
        @Schema(description = "Short-lived JWT access token.")
        String accessToken,

        @Schema(description = "Long-lived refresh token. It is rotated on refresh.")
        String refreshToken,

        @Schema(description = "Token type used in the Authorization header.", example = "Bearer")
        String tokenType,

        @Schema(description = "Access token lifetime in seconds.", example = "900")
        long expiresInSeconds
) {
}
