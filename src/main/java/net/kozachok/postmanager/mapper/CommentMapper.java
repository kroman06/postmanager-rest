package net.kozachok.postmanager.mapper;

import net.kozachok.postmanager.domain.Comment;
import net.kozachok.postmanager.dto.response.CommentResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    @Mapping(target = "authorId",   source = "author.id")
    @Mapping(target = "authorName", expression = "java(comment.getAuthor().getFirstName() + \" \" + comment.getAuthor().getLastName())")
    CommentResponse toResponse(Comment comment);
}