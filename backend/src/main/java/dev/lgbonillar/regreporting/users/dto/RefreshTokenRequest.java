package dev.lgbonillar.regreporting.users.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Refresh token rotation request.")
public record RefreshTokenRequest(
        @Schema(description = "Current refresh token issued by login or previous refresh.")
        @NotBlank(message = "Refresh token is required")
        String refreshToken
) {
}
