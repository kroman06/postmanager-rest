package net.kozachok.postmanager.service.impl;

import lombok.RequiredArgsConstructor;
import net.kozachok.postmanager.domain.CurrentUser;
import net.kozachok.postmanager.dto.response.CommentResponse;
import net.kozachok.postmanager.mapper.CommentMapper;
import net.kozachok.postmanager.repository.ArticleRepository;
import net.kozachok.postmanager.repository.CommentRepository;
import net.kozachok.postmanager.service.CommentService;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final ArticleRepository articleRepository;
    private final CommentMapper commentMapper;

    @Override
    public CommentResponse create(String content, UUID articleId, CurrentUser currentUser) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public CommentResponse update(UUID id, String content, CurrentUser currentUser) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void delete(UUID id, CurrentUser currentUser) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
