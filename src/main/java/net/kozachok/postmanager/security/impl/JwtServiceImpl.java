package net.kozachok.postmanager.security.impl;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import net.kozachok.postmanager.config.JwtProperties;
import net.kozachok.postmanager.domain.User;
import net.kozachok.postmanager.security.JwtService;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HexFormat;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {
    private final JwtProperties jwtProperties;

    private SecretKey signingKey() {
        byte[] bytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(bytes);
    }

    @Override
    public String generateAccessToken(User user) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("roles", user.getRoles().stream()
                        .map(r -> r.getName().name())
                        .toList())
                .issuedAt(new Date(now))
                .expiration(new Date(now + jwtProperties.getAccessExpiration().toMillis()))
                .signWith(signingKey())
                .compact();
    }

    @Override
    public String generateRefreshToken(User user) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(user.getId().toString())
                .issuedAt(new Date(now))
                .expiration(new Date(now + jwtProperties.getRefreshExpiration().toMillis()))
                .signWith(signingKey())
                .compact();
    }

    @Override
    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(signingKey()).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public UUID extractUserId(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return UUID.fromString(claims.getSubject());
    }

    @Override
    public String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}