package net.kozachok.postmanager.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends ArticleApiException {

    public ResourceNotFoundException(String resource, Object id) {
        super(resource + " not found with id: " + id, HttpStatus.NOT_FOUND);
    }
}
