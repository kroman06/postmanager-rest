package net.kozachok.postmanager.service.impl;

import lombok.RequiredArgsConstructor;
import net.kozachok.postmanager.domain.CurrentUser;
import net.kozachok.postmanager.dto.request.ArticleRequest;
import net.kozachok.postmanager.dto.response.ArticleResponse;
import net.kozachok.postmanager.dto.response.PageResponse;
import net.kozachok.postmanager.mapper.ArticleMapper;
import net.kozachok.postmanager.repository.ArticleRepository;
import net.kozachok.postmanager.repository.CategoryRepository;
import net.kozachok.postmanager.service.ArticleService;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {
    private final ArticleRepository articleRepository;
    private final CategoryRepository categoryRepository;
    private final ArticleMapper articleMapper;

    @Override
    public ArticleResponse create(ArticleRequest request, CurrentUser currentUser) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public ArticleResponse update(UUID id, ArticleRequest request, CurrentUser currentUser) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public ArticleResponse publish(UUID id, CurrentUser currentUser) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public ArticleResponse archive(UUID id, CurrentUser currentUser) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public ArticleResponse restore(UUID id, CurrentUser currentUser) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void delete(UUID id, CurrentUser currentUser) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public ArticleResponse findById(UUID id) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public PageResponse<ArticleResponse> findPublished(Pageable pageable) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public PageResponse<ArticleResponse> findByAuthor(UUID authorId, Pageable pageable) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
