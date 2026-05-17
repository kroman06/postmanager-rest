package net.kozachok.postmanager.service.impl;

import lombok.RequiredArgsConstructor;
import net.kozachok.postmanager.domain.*;
import net.kozachok.postmanager.dto.request.ArticleRequest;
import net.kozachok.postmanager.dto.response.ArticleResponse;
import net.kozachok.postmanager.dto.response.PageResponse;
import net.kozachok.postmanager.exception.*;
import net.kozachok.postmanager.mapper.ArticleMapper;
import net.kozachok.postmanager.repository.ArticleRepository;
import net.kozachok.postmanager.repository.CategoryRepository;
import net.kozachok.postmanager.repository.UserRepository;
import net.kozachok.postmanager.service.ArticleService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ArticleServiceImpl implements ArticleService {

    private final ArticleRepository  articleRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository     userRepository;
    private final ArticleMapper      articleMapper;

    @Override
    public ArticleResponse create(ArticleRequest request, CurrentUser currentUser) {
        User author = userRepository.findById(currentUser.id())
                .orElseThrow(() -> new ResourceNotFoundException("User", currentUser.id()));

        Article article = new Article();
        article.setTitle(request.title());
        article.setContent(request.content());
        article.setStatus(ArticleStatus.DRAFT);
        article.setAuthor(author);
        article.setCategory(resolveCategory(request.categoryId()));

        return articleMapper.toResponse(articleRepository.save(article));
    }

    @Transactional(readOnly = true)
    @Override
    public ArticleResponse findMyById(UUID id, CurrentUser currentUser) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Article", id));

        if (!article.isOwnedBy(currentUser.id())) {
            throw new AccessForbiddenException();
        }

        return articleMapper.toResponse(article);
    }

    @Override
    public ArticleResponse update(UUID id, ArticleRequest request, CurrentUser currentUser) {
        Article article = findOrThrow(id);

        if (!article.isOwnedBy(currentUser.id())) {
            throw new AccessForbiddenException();
        }

        // note: Редагування дозволено для будь-якого статусу.
        // note: Якщо стаття опублікована - зміни одразу відображаються для читачів

        article.setTitle(request.title());
        article.setContent(request.content());
        article.setCategory(resolveCategory(request.categoryId()));

        return articleMapper.toResponse(articleRepository.save(article));
    }

    @Override
    public ArticleResponse publish(UUID id, CurrentUser currentUser) {
        Article article = findOrThrow(id);

        if (!article.isOwnedBy(currentUser.id())) {
            throw new AccessForbiddenException();
        }

        if (!article.canPublish()) {
            throw new InvalidStatusTransitionException(article.getStatus(), ArticleStatus.PUBLISHED);
        }

        article.setStatus(ArticleStatus.PUBLISHED);
        article.setPublishedAt(LocalDateTime.now());

        return articleMapper.toResponse(articleRepository.save(article));
    }

    @Override
    public ArticleResponse archive(UUID id, CurrentUser currentUser) {
        Article article = findOrThrow(id);

        if (!currentUser.isAdmin() && !article.isOwnedBy(currentUser.id())) {
            throw new AccessForbiddenException();
        }

        if (!article.canArchive()) {
            throw new InvalidStatusTransitionException(article.getStatus(), ArticleStatus.ARCHIVED);
        }

        article.setStatus(ArticleStatus.ARCHIVED);

        return articleMapper.toResponse(articleRepository.save(article));
    }

    @Override
    public ArticleResponse restore(UUID id, CurrentUser currentUser) {
        Article article = findOrThrow(id);

        if (!currentUser.isAdmin() && !article.isOwnedBy(currentUser.id())) {
            throw new AccessForbiddenException();
        }

        if (!article.canRestore()) {
            throw new InvalidStatusTransitionException(article.getStatus(), ArticleStatus.PUBLISHED);
        }

        article.setStatus(ArticleStatus.PUBLISHED);

        return articleMapper.toResponse(articleRepository.save(article));
    }

    @Override
    public void delete(UUID id, CurrentUser currentUser) {
        Article article = findOrThrow(id);

        if (currentUser.isAdmin()) {
            articleRepository.delete(article);
            return;
        }

        if (!article.isOwnedBy(currentUser.id())) {
            throw new AccessForbiddenException();
        }

        if (!article.isDraft()) {
            throw new InvalidStatusTransitionException(article.getStatus(), ArticleStatus.DRAFT);
        }

        articleRepository.delete(article);
    }

    @Override
    @Transactional(readOnly = true)
    public ArticleResponse findById(UUID id) {
        return articleMapper.toResponse(
                articleRepository.findByIdAndStatus(id, ArticleStatus.PUBLISHED)
                        .orElseThrow(() -> new ResourceNotFoundException("Article", id))
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ArticleResponse> findPublished(Pageable pageable) {
        return toPage(articleRepository.findAllByStatus(ArticleStatus.PUBLISHED, pageable));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ArticleResponse> findByAuthor(UUID authorId, Pageable pageable) {
        return toPage(articleRepository.findAllByAuthorId(authorId, pageable));
    }

    private Article findOrThrow(UUID id) {
        return articleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Article", id));
    }

    private Category resolveCategory(Integer categoryId) {
        if (categoryId == null) return null;
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", categoryId));
    }

    private PageResponse<ArticleResponse> toPage(Page<Article> page) {
        return new PageResponse<>(
                page.getContent().stream().map(articleMapper::toResponse).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }
}