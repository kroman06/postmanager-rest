package net.kozachok.postmanager.exception;

import net.kozachok.postmanager.domain.ArticleStatus;
import org.springframework.http.HttpStatus;

public class InvalidStatusTransitionException extends ArticleApiException {

    public InvalidStatusTransitionException(ArticleStatus from, ArticleStatus to) {
        super("Cannot transition from " + from + " to " + to, HttpStatus.BAD_REQUEST);
    }
}
