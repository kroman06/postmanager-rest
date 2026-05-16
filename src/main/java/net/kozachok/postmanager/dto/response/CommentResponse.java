package net.kozachok.postmanager.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record CommentResponse(
        UUID id,
        String content,
        UUID authorId,
        String authorName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
