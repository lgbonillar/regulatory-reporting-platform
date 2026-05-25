package dev.lgbonillar.regreporting.users.controller;

import dev.lgbonillar.regreporting.shared.ApiResponse;
import dev.lgbonillar.regreporting.users.application.AuthService;
import dev.lgbonillar.regreporting.users.application.CurrentUserProvider;
import dev.lgbonillar.regreporting.users.dto.AuthResponse;
import dev.lgbonillar.regreporting.users.dto.LoginRequest;
import dev.lgbonillar.regreporting.users.dto.LogoutRequest;
import dev.lgbonillar.regreporting.users.dto.RefreshTokenRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(
        name = "Authentication",
        description = "JWT authentication, refresh token rotation and logout operations."
)
public class AuthController {

    private static final String USER_AGENT_HEADER = "User-Agent";

    private final AuthService authService;
    private final CurrentUserProvider currentUserProvider;

    public AuthController(
            AuthService authService,
            CurrentUserProvider currentUserProvider
    ) {
        this.authService = authService;
        this.currentUserProvider = currentUserProvider;
    }

    @PostMapping("/login")
    @SecurityRequirements
    @Operation(
            summary = "Authenticate user",
            description = """
                    Validates username and password, creates a new backend session and returns
                    a short-lived JWT access token plus a rotated refresh token. If the user
                    already has an active session, the previous session is revoked.
                    """,
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "User authenticated successfully",
                            content = @Content(schema = @Schema(implementation = AuthResponse.class))
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "400",
                            description = "Invalid request payload"
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "403",
                            description = "Invalid credentials or inactive user"
                    )
            }
    )
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpServletRequest
    ) {
        AuthResponse response = authService.login(
                request.username(),
                request.password(),
                httpServletRequest.getHeader(USER_AGENT_HEADER),
                httpServletRequest.getRemoteAddr()
        );

        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @PostMapping("/refresh")
    @SecurityRequirements
    @Operation(
            summary = "Refresh access token",
            description = """
                    Rotates a valid refresh token and returns a new access token plus a new
                    refresh token linked to the same backend session.
                    """,
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Token refreshed successfully",
                            content = @Content(schema = @Schema(implementation = AuthResponse.class))
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "400",
                            description = "Invalid request payload"
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "403",
                            description = "Invalid refresh token"
                    )
            }
    )
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            @Valid @RequestBody RefreshTokenRequest request
    ) {
        AuthResponse response = authService.refresh(request.refreshToken());

        return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully", response));
    }

    @PostMapping("/logout")
    @Operation(
            summary = "Logout current user",
            description = """
                    Revokes the active session associated with the provided refresh token.
                    The request must include a valid JWT access token.
                    """,
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "204",
                            description = "Session revoked successfully"
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "400",
                            description = "Invalid request payload"
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = "Missing or invalid JWT"
                    )
            }
    )
    public ResponseEntity<Void> logout(
            @Valid @RequestBody LogoutRequest request
    ) {
        authService.logout(
                request.refreshToken(),
                currentUserProvider.getCurrentUser()
        );

        return ResponseEntity.noContent().build();
    }

}
