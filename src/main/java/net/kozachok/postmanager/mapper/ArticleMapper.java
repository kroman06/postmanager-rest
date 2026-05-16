package net.kozachok.postmanager.mapper;

import net.kozachok.postmanager.domain.Article;
import net.kozachok.postmanager.dto.response.ArticleResponse;

public interface ArticleMapper {
    ArticleResponse toResponse(Article article);
}