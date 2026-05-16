package net.kozachok.postmanager.service.impl;

import lombok.RequiredArgsConstructor;
import net.kozachok.postmanager.dto.request.CategoryRequest;
import net.kozachok.postmanager.dto.response.CategoryResponse;
import net.kozachok.postmanager.mapper.CategoryMapper;
import net.kozachok.postmanager.repository.CategoryRepository;
import net.kozachok.postmanager.service.CategoryService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public CategoryResponse create(CategoryRequest request) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public CategoryResponse update(Integer id, CategoryRequest request) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void delete(Integer id) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public List<CategoryResponse> findAll() {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
