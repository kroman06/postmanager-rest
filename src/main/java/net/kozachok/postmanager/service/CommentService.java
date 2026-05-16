package net.kozachok.postmanager.service;


import net.kozachok.postmanager.domain.CurrentUser;
import net.kozachok.postmanager.dto.response.CommentResponse;

import java.util.UUID;

public interface CommentService {
    CommentResponse create(String content, UUID articleId, CurrentUser currentUser);
    CommentResponse update(UUID id, String content, CurrentUser currentUser);
    void delete(UUID id, CurrentUser currentUser);
}
