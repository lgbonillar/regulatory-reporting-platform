package dev.lgbonillar.regreporting.users.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Logout request used to revoke a refresh token session.")
public record LogoutRequest(
        @Schema(description = "Refresh token to revoke.")
        @NotBlank(message = "Refresh token is required")
        String refreshToken
) {
}
