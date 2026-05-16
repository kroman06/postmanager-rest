package net.kozachok.postmanager.mapper;

import net.kozachok.postmanager.domain.Category;
import net.kozachok.postmanager.dto.response.CategoryResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    CategoryResponse toResponse(Category category);
}