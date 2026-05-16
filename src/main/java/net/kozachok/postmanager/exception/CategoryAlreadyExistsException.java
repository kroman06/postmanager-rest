package net.kozachok.postmanager.exception;

import org.springframework.http.HttpStatus;

public class CategoryAlreadyExistsException extends ArticleApiException {

    public CategoryAlreadyExistsException(String name) {
        super("Category already exists: " + name, HttpStatus.CONFLICT);
    }
}
