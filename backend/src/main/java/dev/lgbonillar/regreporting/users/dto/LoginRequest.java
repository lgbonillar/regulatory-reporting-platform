package dev.lgbonillar.regreporting.users.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Credentials used to authenticate a user.")
public record LoginRequest(
        @Schema(description = "Application username.", example = "analyst01")
        @NotBlank(message = "Username is required")
        String username,

        @Schema(description = "User password.", example = "password")
        @NotBlank(message = "Password is required")
        String password
) {
}
