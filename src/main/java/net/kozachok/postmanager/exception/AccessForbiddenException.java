package net.kozachok.postmanager.exception;

import org.springframework.http.HttpStatus;

public class AccessForbiddenException extends ArticleApiException {

    public AccessForbiddenException() {
        super("Access denied", HttpStatus.FORBIDDEN);
    }
}
