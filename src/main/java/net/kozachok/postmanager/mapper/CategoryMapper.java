package net.kozachok.postmanager.mapper;

import net.kozachok.postmanager.domain.Category;
import net.kozachok.postmanager.dto.response.CategoryResponse;

public interface CategoryMapper {
    CategoryResponse toResponse(Category category);
}