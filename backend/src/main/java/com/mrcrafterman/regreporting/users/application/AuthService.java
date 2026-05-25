package com.mrcrafterman.regreporting.users.application;

import com.mrcrafterman.regreporting.config.JwtProperties;
import com.mrcrafterman.regreporting.shared.ForbiddenOperationException;
import com.mrcrafterman.regreporting.users.domain.AuthSession;
import com.mrcrafterman.regreporting.users.domain.User;
import com.mrcrafterman.regreporting.users.domain.UserStatus;
import com.mrcrafterman.regreporting.users.dto.AuthResponse;
import com.mrcrafterman.regreporting.users.infrastructure.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final String TOKEN_TYPE = "Bearer";
    private static final long SECONDS_PER_MINUTE = 60L;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final RefreshTokenService refreshTokenService;
    private final JwtProperties jwtProperties;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenService jwtTokenService,
            RefreshTokenService refreshTokenService,
            JwtProperties jwtProperties
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
        this.refreshTokenService = refreshTokenService;
        this.jwtProperties = jwtProperties;
    }

    @Transactional
    public AuthResponse login(
            String username,
            String password,
            String userAgent,
            String ipAddress
    ) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ForbiddenOperationException("Invalid credentials"));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new ForbiddenOperationException("User is not active");
        }

        if (user.getPasswordHash() == null ||
                !passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new ForbiddenOperationException("Invalid credentials");
        }

        RefreshTokenService.CreatedRefreshToken createdRefreshToken =
                refreshTokenService.createSession(user, userAgent, ipAddress);

        return createAuthResponse(user, createdRefreshToken);
    }

    @Transactional
    public AuthResponse refresh(String refreshToken) {
        RefreshTokenService.CreatedRefreshToken createdRefreshToken =
                refreshTokenService.rotate(refreshToken);

        AuthSession session = createdRefreshToken.session();

        return createAuthResponse(session.getUser(), createdRefreshToken);
    }

    @Transactional
    public void logout(String refreshToken, User currentUser) {
        refreshTokenService.revoke(refreshToken, currentUser, "User logout");
    }

    private AuthResponse createAuthResponse(
            User user,
            RefreshTokenService.CreatedRefreshToken createdRefreshToken
    ) {
        String accessToken = jwtTokenService.createAccessToken(
                user,
                createdRefreshToken.session().getId()
        );

        return new AuthResponse(
                accessToken,
                createdRefreshToken.refreshToken(),
                TOKEN_TYPE,
                jwtProperties.accessTokenExpirationMinutes() * SECONDS_PER_MINUTE
        );
    }

}
