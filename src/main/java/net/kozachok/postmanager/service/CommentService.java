package net.kozachok.postmanager.service;


import net.kozachok.postmanager.domain.CurrentUser;
import net.kozachok.postmanager.dto.response.CommentResponse;
import net.kozachok.postmanager.dto.response.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface CommentService {
    PageResponse<CommentResponse> findByArticleId(UUID articleId, Pageable pageable);
    CommentResponse create(String content, UUID articleId, CurrentUser currentUser);
    void delete(UUID id, CurrentUser currentUser);
}
