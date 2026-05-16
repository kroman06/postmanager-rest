package net.kozachok.postmanager.mapper.impl;

import net.kozachok.postmanager.domain.Article;
import net.kozachok.postmanager.dto.response.ArticleResponse;
import net.kozachok.postmanager.mapper.ArticleMapper;
import org.springframework.stereotype.Component;

@Component
public class ArticleMapperImpl implements ArticleMapper {
    @Override
    public ArticleResponse toResponse(Article article) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}