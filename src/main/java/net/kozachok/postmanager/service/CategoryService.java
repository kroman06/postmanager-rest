package net.kozachok.postmanager.service;

import net.kozachok.postmanager.dto.request.CategoryRequest;
import net.kozachok.postmanager.dto.response.CategoryResponse;

import java.util.List;

public interface CategoryService {
    CategoryResponse create(CategoryRequest request);
    CategoryResponse update(Integer id, CategoryRequest request);
    void delete(Integer id);
    List<CategoryResponse> findAll();
}
