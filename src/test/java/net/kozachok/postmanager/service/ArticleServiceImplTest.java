package net.kozachok.postmanager.service;

import net.kozachok.postmanager.domain.*;
import net.kozachok.postmanager.dto.request.ArticleRequest;
import net.kozachok.postmanager.dto.response.ArticleResponse;
import net.kozachok.postmanager.exception.AccessForbiddenException;
import net.kozachok.postmanager.exception.InvalidStatusTransitionException;
import net.kozachok.postmanager.exception.ResourceNotFoundException;
import net.kozachok.postmanager.mapper.ArticleMapper;
import net.kozachok.postmanager.repository.ArticleRepository;
import net.kozachok.postmanager.repository.CategoryRepository;
import net.kozachok.postmanager.repository.UserRepository;
import net.kozachok.postmanager.service.impl.ArticleServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ArticleServiceImplTest {
    @Mock private UserRepository userRepository;
    @Mock private ArticleRepository articleRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private ArticleMapper articleMapper;
    @InjectMocks private ArticleServiceImpl articleService;

    private final UUID ARTICLE_ID = UUID.randomUUID();
    private final UUID AUTHOR_ID  = UUID.randomUUID();

    private CurrentUser authorUser() {
        return new CurrentUser(AUTHOR_ID, Set.of(RoleName.ROLE_AUTHOR));
    }

    private CurrentUser adminUser() {
        return new CurrentUser(UUID.randomUUID(), Set.of(RoleName.ROLE_ADMIN));
    }

    private CurrentUser strangerUser() {
        return new CurrentUser(UUID.randomUUID(), Set.of(RoleName.ROLE_AUTHOR));
    }

    private Article article(ArticleStatus status) {
        User author = new User();
        author.setId(AUTHOR_ID);

        Article article = new Article();
        article.setStatus(status);
        article.setTitle("Test Title");
        article.setContent("Test Content");
        article.setAuthor(author);
        return article;
    }

    // ── publish ──────────────────────────────────────────────

    @Test
    void publish_shouldSetStatusPublished_whenDraftAndOwner() {
        Article art = article(ArticleStatus.DRAFT);

        when(articleRepository.findById(ARTICLE_ID)).thenReturn(Optional.of(art));
        when(articleRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(articleMapper.toResponse(any())).thenReturn(mock(ArticleResponse.class));

        articleService.publish(ARTICLE_ID, authorUser());

        verify(articleRepository).save(argThat(a ->
                a.getStatus() == ArticleStatus.PUBLISHED &&
                        a.getPublishedAt() != null
        ));
    }

    @Test
    void publish_shouldThrowNotFound_whenArticleNotExists() {
        when(articleRepository.findById(ARTICLE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> articleService.publish(ARTICLE_ID, authorUser()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void publish_shouldThrowForbidden_whenNotOwner() {
        when(articleRepository.findById(ARTICLE_ID)).thenReturn(Optional.of(article(ArticleStatus.DRAFT)));

        assertThatThrownBy(() -> articleService.publish(ARTICLE_ID, strangerUser()))
                .isInstanceOf(AccessForbiddenException.class);
    }

    @Test
    void publish_shouldThrowForbidden_whenAdmin() {
        // business-rule: even admin  can't publish articles of other users
        when(articleRepository.findById(ARTICLE_ID)).thenReturn(Optional.of(article(ArticleStatus.DRAFT)));

        assertThatThrownBy(() -> articleService.publish(ARTICLE_ID, adminUser()))
                .isInstanceOf(AccessForbiddenException.class);
    }

    @Test
    void publish_shouldThrowInvalidTransition_whenNotDraft() {
        when(articleRepository.findById(ARTICLE_ID)).thenReturn(Optional.of(article(ArticleStatus.PUBLISHED)));

        assertThatThrownBy(() -> articleService.publish(ARTICLE_ID, authorUser()))
                .isInstanceOf(InvalidStatusTransitionException.class);
    }

    // ── archive ──────────────────────────────────────────────

    @Test
    void archive_shouldSetStatusArchived_whenPublishedAndOwner() {
        when(articleRepository.findById(ARTICLE_ID)).thenReturn(Optional.of(article(ArticleStatus.PUBLISHED)));
        when(articleRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(articleMapper.toResponse(any())).thenReturn(mock(ArticleResponse.class));

        articleService.archive(ARTICLE_ID, authorUser());

        verify(articleRepository).save(argThat(a -> a.getStatus() == ArticleStatus.ARCHIVED));
    }

    @Test
    void archive_shouldSucceed_whenAdminArchivesAnyArticle() {
        when(articleRepository.findById(ARTICLE_ID)).thenReturn(Optional.of(article(ArticleStatus.PUBLISHED)));
        when(articleRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(articleMapper.toResponse(any())).thenReturn(mock(ArticleResponse.class));

        assertThatNoException().isThrownBy(() -> articleService.archive(ARTICLE_ID, adminUser()));
        verify(articleRepository).save(argThat(a -> a.getStatus() == ArticleStatus.ARCHIVED));
    }

    @Test
    void archive_shouldThrowForbidden_whenNotOwnerAndNotAdmin() {
        when(articleRepository.findById(ARTICLE_ID)).thenReturn(Optional.of(article(ArticleStatus.PUBLISHED)));

        assertThatThrownBy(() -> articleService.archive(ARTICLE_ID, strangerUser()))
                .isInstanceOf(AccessForbiddenException.class);
    }

    @Test
    void archive_shouldThrowInvalidTransition_whenStatusIsDraft() {
        when(articleRepository.findById(ARTICLE_ID)).thenReturn(Optional.of(article(ArticleStatus.DRAFT)));

        assertThatThrownBy(() -> articleService.archive(ARTICLE_ID, authorUser()))
                .isInstanceOf(InvalidStatusTransitionException.class);
    }

    @Test
    void archive_shouldThrowNotFound_whenArticleNotExists() {
        when(articleRepository.findById(ARTICLE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> articleService.archive(ARTICLE_ID, authorUser()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── restore ──────────────────────────────────────────────

    @Test
    void restore_shouldSetStatusPublished_whenArchivedAndOwner() {
        when(articleRepository.findById(ARTICLE_ID)).thenReturn(Optional.of(article(ArticleStatus.ARCHIVED)));
        when(articleRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(articleMapper.toResponse(any())).thenReturn(mock(ArticleResponse.class));

        articleService.restore(ARTICLE_ID, authorUser());

        verify(articleRepository).save(argThat(a -> a.getStatus() == ArticleStatus.PUBLISHED));
    }

    @Test
    void restore_shouldSucceed_whenAdminRestoresAnyArticle() {
        when(articleRepository.findById(ARTICLE_ID)).thenReturn(Optional.of(article(ArticleStatus.ARCHIVED)));
        when(articleRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(articleMapper.toResponse(any())).thenReturn(mock(ArticleResponse.class));

        assertThatNoException().isThrownBy(() -> articleService.restore(ARTICLE_ID, adminUser()));
    }

    @Test
    void restore_shouldThrowForbidden_whenNotOwnerAndNotAdmin() {
        when(articleRepository.findById(ARTICLE_ID)).thenReturn(Optional.of(article(ArticleStatus.ARCHIVED)));

        assertThatThrownBy(() -> articleService.restore(ARTICLE_ID, strangerUser()))
                .isInstanceOf(AccessForbiddenException.class);
    }

    @Test
    void restore_shouldThrowInvalidTransition_whenNotArchived() {
        when(articleRepository.findById(ARTICLE_ID)).thenReturn(Optional.of(article(ArticleStatus.PUBLISHED)));

        assertThatThrownBy(() -> articleService.restore(ARTICLE_ID, authorUser()))
                .isInstanceOf(InvalidStatusTransitionException.class);
    }

    // ── delete ───────────────────────────────────────────────

    @Test
    void delete_shouldDelete_whenOwnerAndDraft() {
        Article art = article(ArticleStatus.DRAFT);

        when(articleRepository.findById(ARTICLE_ID)).thenReturn(Optional.of(art));

        articleService.delete(ARTICLE_ID, authorUser());

        verify(articleRepository).delete(art);
    }

    @Test
    void delete_shouldSucceed_whenAdminDeletesAnyArticle() {
        Article art = article(ArticleStatus.PUBLISHED);

        when(articleRepository.findById(ARTICLE_ID)).thenReturn(Optional.of(art));

        assertThatNoException().isThrownBy(() -> articleService.delete(ARTICLE_ID, adminUser()));
        verify(articleRepository).delete(art);
    }

    @Test
    void delete_shouldThrowInvalidTransition_whenOwnerTriesToDeletePublished() {
        when(articleRepository.findById(ARTICLE_ID)).thenReturn(Optional.of(article(ArticleStatus.PUBLISHED)));

        assertThatThrownBy(() -> articleService.delete(ARTICLE_ID, authorUser()))
                .isInstanceOf(InvalidStatusTransitionException.class);

        verify(articleRepository, never()).delete(any());
    }

    @Test
    void delete_shouldThrowForbidden_whenNotOwnerAndNotAdmin() {
        when(articleRepository.findById(ARTICLE_ID)).thenReturn(Optional.of(article(ArticleStatus.DRAFT)));

        assertThatThrownBy(() -> articleService.delete(ARTICLE_ID, strangerUser()))
                .isInstanceOf(AccessForbiddenException.class);

        verify(articleRepository, never()).delete(any());
    }

    @Test
    void delete_shouldThrowNotFound_whenArticleNotExists() {
        when(articleRepository.findById(ARTICLE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> articleService.delete(ARTICLE_ID, authorUser()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── create ───────────────────────────────────────────────

    @Test
    void create_shouldSaveArticleWithDraftStatus() {
        ArticleRequest request = new ArticleRequest("Title", "Content", null);

        User author = new User();
        author.setId(AUTHOR_ID);
        when(userRepository.findById(AUTHOR_ID)).thenReturn(Optional.of(author));
        when(articleRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(articleMapper.toResponse(any())).thenReturn(mock(ArticleResponse.class));

        articleService.create(request, authorUser());

        verify(articleRepository).save(argThat(a ->
                a.getStatus() == ArticleStatus.DRAFT &&
                        a.getTitle().equals("Title")
        ));
    }

    @Test
    void create_shouldThrowNotFound_whenCategoryNotExists() {
        ArticleRequest request = new ArticleRequest("Title", "Content", 999);

        User author = new User();
        author.setId(AUTHOR_ID);
        when(userRepository.findById(AUTHOR_ID)).thenReturn(Optional.of(author));
        when(categoryRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> articleService.create(request, authorUser()))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(articleRepository, never()).save(any());
    }

    // ── update ───────────────────────────────────────────────

    @Test
    void update_shouldThrowForbidden_whenNotOwner() {
        when(articleRepository.findById(ARTICLE_ID)).thenReturn(Optional.of(article(ArticleStatus.DRAFT)));

        assertThatThrownBy(() -> articleService.update(ARTICLE_ID, new ArticleRequest("T", "C", null), strangerUser()))
                .isInstanceOf(AccessForbiddenException.class);
    }

    @Test
    void update_shouldThrowNotFound_whenArticleNotExists() {
        when(articleRepository.findById(ARTICLE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> articleService.update(ARTICLE_ID, new ArticleRequest("T", "C", null), authorUser()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void update_shouldThrowNotFound_whenCategoryNotExists() {
        when(articleRepository.findById(ARTICLE_ID)).thenReturn(Optional.of(article(ArticleStatus.DRAFT)));
        when(categoryRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> articleService.update(ARTICLE_ID, new ArticleRequest("T", "C", 99), authorUser()))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}