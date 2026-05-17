package net.kozachok.postmanager.service.impl;

import lombok.RequiredArgsConstructor;
import net.kozachok.postmanager.domain.*;
import net.kozachok.postmanager.dto.response.CommentResponse;
import net.kozachok.postmanager.dto.response.PageResponse;
import net.kozachok.postmanager.exception.AccessForbiddenException;
import net.kozachok.postmanager.exception.ResourceNotFoundException;
import net.kozachok.postmanager.mapper.CommentMapper;
import net.kozachok.postmanager.repository.ArticleRepository;
import net.kozachok.postmanager.repository.CommentRepository;
import net.kozachok.postmanager.repository.UserRepository;
import net.kozachok.postmanager.service.CommentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final ArticleRepository articleRepository;
    private final UserRepository    userRepository;
    private final CommentMapper     commentMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CommentResponse> findByArticleId(UUID articleId, Pageable pageable) {
        if (!articleRepository.existsById(articleId)) {
            throw new ResourceNotFoundException("Article", articleId);
        }
        Page<Comment> page = commentRepository.findAllByArticleId(articleId, pageable);
        return new PageResponse<>(
                page.getContent().stream().map(commentMapper::toResponse).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }

    @Override
    public CommentResponse create(String content, UUID articleId, CurrentUser currentUser) {
        Article article = articleRepository.findByIdAndStatus(articleId, ArticleStatus.PUBLISHED)
                .orElseThrow(() -> new ResourceNotFoundException("Article", articleId));

        User author = userRepository.findById(currentUser.id())
                .orElseThrow(() -> new ResourceNotFoundException("User", currentUser.id()));

        Comment comment = new Comment();
        comment.setContent(content);
        comment.setArticle(article);
        comment.setAuthor(author);

        return commentMapper.toResponse(commentRepository.save(comment));
    }

    @Override
    public void delete(UUID id, CurrentUser currentUser) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", id));

        if (!currentUser.isAdmin()) {
            throw new AccessForbiddenException();
        }

        commentRepository.delete(comment);
    }
}