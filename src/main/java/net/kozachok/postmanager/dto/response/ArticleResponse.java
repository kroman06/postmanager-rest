package net.kozachok.postmanager.dto.response;

import net.kozachok.postmanager.domain.ArticleStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record ArticleResponse(
        UUID id,
        String title,
        String content,
        ArticleStatus status,
        UUID authorId,
        String authorName,
        Integer categoryId,
        String categoryName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime publishedAt
) {}
