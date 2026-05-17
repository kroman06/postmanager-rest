package net.kozachok.postmanager.security.impl;

import io.github.cdimascio.dotenv.Dotenv;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import net.kozachok.postmanager.config.JwtProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.util.Date;
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
}