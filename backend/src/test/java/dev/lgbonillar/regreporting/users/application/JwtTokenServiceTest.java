package dev.lgbonillar.regreporting.users.application;

import dev.lgbonillar.regreporting.config.JwtProperties;
import dev.lgbonillar.regreporting.users.domain.Role;
import dev.lgbonillar.regreporting.users.domain.User;
import dev.lgbonillar.regreporting.users.domain.UserRole;
import dev.lgbonillar.regreporting.users.domain.UserStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtTokenServiceTest {

    @Mock
    private JwtEncoder jwtEncoder;

    private final JwtProperties jwtProperties =
            new JwtProperties("secret", "regulatory-reporting-api", 15L, 7L);

    @Test
    void createAccessTokenIncludesUserRoleAndSessionClaims() {
        User user = user(UserRole.ANALYST);
        UUID sessionId = UUID.randomUUID();
        JwtTokenService jwtTokenService = new JwtTokenService(jwtEncoder, jwtProperties);

        when(jwtEncoder.encode(org.mockito.ArgumentMatchers.any(JwtEncoderParameters.class)))
                .thenReturn(Jwt.withTokenValue("access-token")
                        .header("alg", "HS256")
                        .claim("sub", user.getId().toString())
                        .build());

        String result = jwtTokenService.createAccessToken(user, sessionId);

        assertThat(result).isEqualTo("access-token");

        ArgumentCaptor<JwtEncoderParameters> captor =
                ArgumentCaptor.forClass(JwtEncoderParameters.class);
        verify(jwtEncoder).encode(captor.capture());

        JwtEncoderParameters parameters = captor.getValue();

        assertThat(parameters.getJwsHeader().getAlgorithm().getName()).isEqualTo("HS256");
        assertThat((String) parameters.getClaims().getClaim("iss")).isEqualTo("regulatory-reporting-api");
        assertThat(parameters.getClaims().getSubject()).isEqualTo(user.getId().toString());
        assertThat((String) parameters.getClaims().getClaim("userId")).isEqualTo(user.getId().toString());
        assertThat((String) parameters.getClaims().getClaim("username")).isEqualTo("analyst01");
        assertThat((String) parameters.getClaims().getClaim("role")).isEqualTo(UserRole.ANALYST.name());
        assertThat((String) parameters.getClaims().getClaim("sessionId")).isEqualTo(sessionId.toString());
        assertThat(Duration.between(
                parameters.getClaims().getIssuedAt(),
                parameters.getClaims().getExpiresAt()
        )).isEqualTo(Duration.ofMinutes(15L));
    }

    @Test
    void createAccessTokenUsesHighestPriorityRole() {
        User user = user(UserRole.AUDITOR);
        UUID sessionId = UUID.randomUUID();
        JwtTokenService jwtTokenService = new JwtTokenService(jwtEncoder, jwtProperties);

        user.getRoles().add(role(UserRole.ROOT));

        when(jwtEncoder.encode(org.mockito.ArgumentMatchers.any(JwtEncoderParameters.class)))
                .thenReturn(Jwt.withTokenValue("access-token")
                        .header("alg", "HS256")
                        .claim("sub", user.getId().toString())
                        .build());

        jwtTokenService.createAccessToken(user, sessionId);

        ArgumentCaptor<JwtEncoderParameters> captor =
                ArgumentCaptor.forClass(JwtEncoderParameters.class);
        verify(jwtEncoder).encode(captor.capture());

        assertThat((String) captor.getValue().getClaims().getClaim("role"))
                .isEqualTo(UserRole.ROOT.name());
    }

    private User user(UserRole userRole) {
        User user = new User(
                "analyst01",
                "analyst01@example.com",
                "Analyst 01",
                "password-hash",
                false,
                UserStatus.ACTIVE
        );
        user.setId(UUID.randomUUID());
        user.getRoles().add(role(userRole));
        return user;
    }

    private Role role(UserRole userRole) {
        return new Role(userRole.name(), userRole.name(), null);
    }

}
