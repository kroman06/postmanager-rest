package net.kozachok.postmanager.service;

import net.kozachok.postmanager.domain.ArticleStatus;
import net.kozachok.postmanager.domain.CurrentUser;
import net.kozachok.postmanager.dto.request.ArticleRequest;
import net.kozachok.postmanager.dto.response.ArticleResponse;
import net.kozachok.postmanager.dto.response.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ArticleService {
    ArticleResponse create(ArticleRequest request, CurrentUser currentUser);
    ArticleResponse publish(UUID id, CurrentUser currentUser);
    ArticleResponse update(UUID id, ArticleRequest request, CurrentUser currentUser);
    ArticleResponse archive(UUID id, CurrentUser currentUser);
    ArticleResponse restore(UUID id, CurrentUser currentUser);
    ArticleResponse findMyById(UUID id, CurrentUser currentUser);
    ArticleResponse findById(UUID id);
    PageResponse<ArticleResponse> findPublished(Pageable pageable, Integer categoryId);
    PageResponse<ArticleResponse> findByAuthor(UUID authorId, Pageable pageable);
    PageResponse<ArticleResponse> findAll(Pageable createdAt, ArticleStatus status);
    void delete(UUID id, CurrentUser currentUser);
}
