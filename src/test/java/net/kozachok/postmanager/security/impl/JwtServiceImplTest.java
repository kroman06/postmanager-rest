package net.kozachok.postmanager.security.impl;

import io.github.cdimascio.dotenv.Dotenv;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import net.kozachok.postmanager.config.JwtProperties;
import net.kozachok.postmanager.domain.Role;
import net.kozachok.postmanager.domain.RoleName;
import net.kozachok.postmanager.domain.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtServiceImplTest {
    private static final Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
    @Mock
    private JwtProperties jwtProperties;

    @InjectMocks
    private JwtServiceImpl jwtService;

    @Test
    void validateToken_shouldReturnFalse_whenTokenIsExpired() {
        String secret = dotenv.get("JWT_SECRET", "5a7d3f9b2e1c8d4f6a0b3e7c9d2f1a8b4c6e0f3a7d5b9c1e4f2a6b8d0c3e5f7");
        when(jwtProperties.getSecret()).thenReturn(secret);

        String expiredToken = Jwts.builder()
                .subject(UUID.randomUUID().toString())
                .issuedAt(new Date(System.currentTimeMillis() - 7200000))
                .expiration(new Date(System.currentTimeMillis() - 3600000))
                .signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
                .compact();

        boolean isValid = jwtService.validateToken(expiredToken);

        assertThat(isValid).isFalse();
    }

    @Test
    void validateToken_shouldReturnTrue_whenTokenIsValid() {
        String secret = dotenv.get("JWT_SECRET", "5a7d3f9b2e1c8d4f6a0b3e7c9d2f1a8b4c6e0f3a7d5b9c1e4f2a6b8d0c3e5f7");
        when(jwtProperties.getSecret()).thenReturn(secret);
        when(jwtProperties.getAccessExpiration()).thenReturn(Duration.ofMinutes(15));

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setRoles(Set.of());

        String token = jwtService.generateAccessToken(user);

        assertThat(jwtService.validateToken(token)).isTrue();
    }

    @Test
    void validateToken_shouldReturnFalse_whenTokenIsMalformed() {
        when(jwtProperties.getSecret()).thenReturn(
                "5a7d3f9b2e1c8d4f6a0b3e7c9d2f1a8b4c6e0f3a7d5b9c1e4f2a6b8d0c3e5f7");

        assertThat(jwtService.validateToken("not.a.jwt")).isFalse();
    }

    @Test
    void extractUserId_shouldReturnCorrectId() {
        String secret = dotenv.get("JWT_SECRET", "5a7d3f9b2e1c8d4f6a0b3e7c9d2f1a8b4c6e0f3a7d5b9c1e4f2a6b8d0c3e5f7");
        when(jwtProperties.getSecret()).thenReturn(secret);
        when(jwtProperties.getAccessExpiration()).thenReturn(Duration.ofMinutes(15));

        UUID expectedId = UUID.randomUUID();
        User user = new User();
        user.setId(expectedId);
        user.setRoles(Set.of());

        String token = jwtService.generateAccessToken(user);

        assertThat(jwtService.extractUserId(token)).isEqualTo(expectedId);
    }

    @Test
    void extractRoles_shouldReturnCorrectRoles() {
        String secret = dotenv.get("JWT_SECRET", "5a7d3f9b2e1c8d4f6a0b3e7c9d2f1a8b4c6e0f3a7d5b9c1e4f2a6b8d0c3e5f7");
        when(jwtProperties.getSecret()).thenReturn(secret);
        when(jwtProperties.getAccessExpiration()).thenReturn(Duration.ofMinutes(15));

        Role role = new Role();
        role.setName(RoleName.ROLE_AUTHOR);

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setRoles(Set.of(role));

        String token = jwtService.generateAccessToken(user);

        assertThat(jwtService.extractRoles(token)).containsExactly(RoleName.ROLE_AUTHOR);
    }

    @Test
    void hashToken_shouldReturnConsistentHash() {
        String hash1 = jwtService.hashToken("some.token");
        String hash2 = jwtService.hashToken("some.token");

        assertThat(hash1).isEqualTo(hash2).hasSize(64);
    }
}