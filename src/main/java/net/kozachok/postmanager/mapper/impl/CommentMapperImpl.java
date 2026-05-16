package net.kozachok.postmanager.mapper.impl;

import net.kozachok.postmanager.domain.Comment;
import net.kozachok.postmanager.dto.response.CommentResponse;
import net.kozachok.postmanager.mapper.CommentMapper;
import org.springframework.stereotype.Component;

@Component
public class CommentMapperImpl implements CommentMapper {
    @Override
    public CommentResponse toResponse(Comment comment) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}