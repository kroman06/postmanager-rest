package net.kozachok.postmanager.exception;

import org.springframework.http.HttpStatus;

public class InvalidRefreshTokenException extends ArticleApiException {

    public InvalidRefreshTokenException() {
        super("Refresh token is invalid, expired or revoked", HttpStatus.UNAUTHORIZED);
    }
}
