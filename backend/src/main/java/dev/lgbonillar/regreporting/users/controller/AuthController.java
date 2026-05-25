package dev.lgbonillar.regreporting.users.controller;

import dev.lgbonillar.regreporting.users.application.AuthService;
import dev.lgbonillar.regreporting.users.application.CurrentUserProvider;
import dev.lgbonillar.regreporting.users.dto.AuthResponse;
import dev.lgbonillar.regreporting.users.dto.LoginRequest;
import dev.lgbonillar.regreporting.users.dto.LogoutRequest;
import dev.lgbonillar.regreporting.users.dto.RefreshTokenRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
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
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpServletRequest
    ) {
        return ResponseEntity.ok(authService.login(
                request.username(),
                request.password(),
                httpServletRequest.getHeader(USER_AGENT_HEADER),
                httpServletRequest.getRemoteAddr()
        ));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            @Valid @RequestBody RefreshTokenRequest request
    ) {
        return ResponseEntity.ok(authService.refresh(request.refreshToken()));
    }

    @PostMapping("/logout")
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
