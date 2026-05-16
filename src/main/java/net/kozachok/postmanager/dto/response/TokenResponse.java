package net.kozachok.postmanager.dto.response;

public record TokenResponse(
        String accessToken,
        String refreshToken
) {}
