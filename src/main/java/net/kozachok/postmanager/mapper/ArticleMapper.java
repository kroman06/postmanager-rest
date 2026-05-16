package net.kozachok.postmanager.mapper;

import net.kozachok.postmanager.domain.Article;
import net.kozachok.postmanager.dto.response.ArticleResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ArticleMapper {

    @Mapping(target = "authorId",   source = "author.id")
    @Mapping(target = "authorName", expression = "java(article.getAuthor().getFirstName() + \" \" + article.getAuthor().getLastName())")
    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    ArticleResponse toResponse(Article article);
}