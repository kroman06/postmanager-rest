package net.kozachok.postmanager.dto.response;

import java.time.LocalDateTime;

public record CategoryResponse(
        Integer id,
        String name,
        String description,
        LocalDateTime createdAt
) {}
