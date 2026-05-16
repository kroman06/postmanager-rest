package net.kozachok.postmanager.mapper.impl;

import net.kozachok.postmanager.domain.Category;
import net.kozachok.postmanager.dto.response.CategoryResponse;
import net.kozachok.postmanager.mapper.CategoryMapper;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapperImpl implements CategoryMapper {
    @Override
    public CategoryResponse toResponse(Category category) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}