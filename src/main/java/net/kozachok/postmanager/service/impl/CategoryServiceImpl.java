package net.kozachok.postmanager.service.impl;

import lombok.RequiredArgsConstructor;
import net.kozachok.postmanager.domain.Category;
import net.kozachok.postmanager.dto.request.CategoryRequest;
import net.kozachok.postmanager.dto.response.CategoryResponse;
import net.kozachok.postmanager.exception.CategoryAlreadyExistsException;
import net.kozachok.postmanager.exception.ResourceNotFoundException;
import net.kozachok.postmanager.mapper.CategoryMapper;
import net.kozachok.postmanager.repository.CategoryRepository;
import net.kozachok.postmanager.service.CategoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper     categoryMapper;

    @Override
    public CategoryResponse create(CategoryRequest request) {
        if (categoryRepository.existsByName(request.name())) {
            throw new CategoryAlreadyExistsException(request.name());
        }

        Category category = new Category();
        category.setName(request.name());
        category.setDescription(request.description());

        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    @Override
    public CategoryResponse update(Integer id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));

        if (!category.getName().equals(request.name()) &&
                categoryRepository.existsByName(request.name())) {
            throw new CategoryAlreadyExistsException(request.name());
        }

        category.setName(request.name());
        category.setDescription(request.description());

        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    @Override
    public void delete(Integer id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));
        categoryRepository.delete(category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> findAll() {
        return categoryRepository.findAll().stream()
                .map(categoryMapper::toResponse)
                .toList();
    }
}