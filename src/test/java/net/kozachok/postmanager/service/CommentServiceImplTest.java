package net.kozachok.postmanager.service;

import net.kozachok.postmanager.domain.*;
import net.kozachok.postmanager.dto.response.CommentResponse;
import net.kozachok.postmanager.dto.response.PageResponse;
import net.kozachok.postmanager.exception.AccessForbiddenException;
import net.kozachok.postmanager.exception.ResourceNotFoundException;
import net.kozachok.postmanager.mapper.CommentMapper;
import net.kozachok.postmanager.repository.ArticleRepository;
import net.kozachok.postmanager.repository.CommentRepository;
import net.kozachok.postmanager.repository.UserRepository;
import net.kozachok.postmanager.service.impl.CommentServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceImplTest {
    @Mock private UserRepository userRepository;
    @Mock private CommentRepository commentRepository;
    @Mock private ArticleRepository articleRepository;
    @Mock private CommentMapper commentMapper;
    @InjectMocks private CommentServiceImpl commentService;

    private final UUID COMMENT_ID = UUID.randomUUID();
    private final UUID ARTICLE_ID = UUID.randomUUID();
    private final UUID AUTHOR_ID  = UUID.randomUUID();

    private CurrentUser authorUser() {
        return new CurrentUser(AUTHOR_ID, Set.of(RoleName.ROLE_AUTHOR));
    }

    private CurrentUser adminUser() {
        return new CurrentUser(UUID.randomUUID(), Set.of(RoleName.ROLE_ADMIN));
    }

    // ── find ───────────────────────────────────────────────

    @Test
    void findByArticleId_shouldReturnCommentList_whenArticleExists() {
        Pageable pageable = PageRequest.of(0, 20);

        User author = new User();
        author.setId(UUID.randomUUID());
        author.setFirstName("John");
        author.setLastName("Doe");

        Comment comment = new Comment();
        comment.setContent("Hello");
        comment.setAuthor(author);

        Page<Comment> page = new PageImpl<>(List.of(comment));

        when(articleRepository.existsById(ARTICLE_ID)).thenReturn(true);
        when(commentRepository.findAllByArticleId(ARTICLE_ID, pageable)).thenReturn(page);
        when(commentMapper.toResponse(comment)).thenReturn(mock(CommentResponse.class));

        PageResponse<CommentResponse> result = commentService.findByArticleId(ARTICLE_ID, pageable);

        assertThat(result.content()).hasSize(1);
        verify(commentMapper).toResponse(comment);
    }

    @Test
    void findByArticleId_shouldThrowNotFound_whenArticleNotExists() {
        Pageable pageable = PageRequest.of(0, 20);

        when(articleRepository.existsById(ARTICLE_ID)).thenReturn(false);

        assertThatThrownBy(() -> commentService.findByArticleId(ARTICLE_ID, pageable))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(commentRepository, never()).findAllByArticleId(any(), any());
    }

    // ── create ───────────────────────────────────────────────

    @Test
    void create_shouldThrowNotFound_whenAuthorNotExists() {
        Article article = new Article();
        article.setStatus(ArticleStatus.PUBLISHED);

        when(articleRepository.findByIdAndStatus(ARTICLE_ID, ArticleStatus.PUBLISHED))
                .thenReturn(Optional.of(article));
        when(userRepository.findById(AUTHOR_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.create("text", ARTICLE_ID, authorUser()))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(commentRepository, never()).save(any());
    }

    @Test
    void create_shouldSaveComment_whenArticleIsPublished() {
        Article article = new Article();
        article.setStatus(ArticleStatus.PUBLISHED);

        User author = new User();
        author.setId(AUTHOR_ID);

        when(articleRepository.findByIdAndStatus(ARTICLE_ID, ArticleStatus.PUBLISHED))
                .thenReturn(Optional.of(article));
        when(userRepository.findById(AUTHOR_ID)).thenReturn(Optional.of(author));
        when(commentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(commentMapper.toResponse(any())).thenReturn(mock(CommentResponse.class));

        commentService.create("Great article!", ARTICLE_ID, authorUser());

        verify(commentRepository).save(argThat(c -> c.getContent().equals("Great article!")));
    }

    @Test
    void create_shouldThrowNotFound_whenArticleNotPublished() {
        when(articleRepository.findByIdAndStatus(ARTICLE_ID, ArticleStatus.PUBLISHED))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.create("text", ARTICLE_ID, authorUser()))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(commentRepository, never()).save(any());
    }

    // ── delete ───────────────────────────────────────────────

    @Test
    void delete_shouldDelete_whenAdmin() {
        Comment comment = new Comment();

        when(commentRepository.findById(COMMENT_ID)).thenReturn(Optional.of(comment));

        commentService.delete(COMMENT_ID, adminUser());

        verify(commentRepository).delete(comment);
    }

    @Test
    void delete_shouldThrowForbidden_whenNotAdmin() {
        Comment comment = new Comment();

        when(commentRepository.findById(COMMENT_ID)).thenReturn(Optional.of(comment));

        assertThatThrownBy(() -> commentService.delete(COMMENT_ID, authorUser()))
                .isInstanceOf(AccessForbiddenException.class);

        verify(commentRepository, never()).delete(any());
    }

    @Test
    void delete_shouldThrowNotFound_whenCommentNotExists() {
        when(commentRepository.findById(COMMENT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.delete(COMMENT_ID, adminUser()))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}