package net.kozachok.postmanager.exception;

import org.springframework.http.HttpStatus;

public class EmailAlreadyExistsException extends ArticleApiException {

    public EmailAlreadyExistsException(String email) {
        super("Email already in use: " + email, HttpStatus.CONFLICT);
    }
}
