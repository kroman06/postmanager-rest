package net.kozachok.postmanager.mapper;

import net.kozachok.postmanager.domain.Comment;
import net.kozachok.postmanager.dto.response.CommentResponse;

public interface CommentMapper {
    CommentResponse toResponse(Comment comment);
}