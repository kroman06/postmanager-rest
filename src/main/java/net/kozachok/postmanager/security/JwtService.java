package net.kozachok.postmanager.security;

import net.kozachok.postmanager.domain.User;

import java.util.UUID;

public interface JwtService {
    String generateAccessToken(User user);
    String generateRefreshToken(User user);
    boolean validateToken(String token);
    UUID extractUserId(String token);
    String hashToken(String rawToken);
}