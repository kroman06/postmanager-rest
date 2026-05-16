package net.kozachok.postmanager.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ArticleApiException extends RuntimeException {

    private final HttpStatus status;

    public ArticleApiException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

}
