package net.kozachok.postmanager.service;

import net.kozachok.postmanager.domain.Category;
import net.kozachok.postmanager.dto.request.CategoryRequest;
import net.kozachok.postmanager.dto.response.CategoryResponse;
import net.kozachok.postmanager.exception.CategoryAlreadyExistsException;
import net.kozachok.postmanager.exception.ResourceNotFoundException;
import net.kozachok.postmanager.mapper.CategoryMapper;
import net.kozachok.postmanager.repository.CategoryRepository;
import net.kozachok.postmanager.service.impl.CategoryServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock private CategoryRepository categoryRepository;
    @Mock private CategoryMapper categoryMapper;
    @InjectMocks private CategoryServiceImpl categoryService;

    // ── create ───────────────────────────────────────────────

    @Test
    void create_shouldSaveCategory_whenNameNotTaken() {
        CategoryRequest request = new CategoryRequest("Technology", "Tech articles");

        when(categoryRepository.existsByName("Technology")).thenReturn(false);
        when(categoryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(categoryMapper.toResponse(any())).thenReturn(mock(CategoryResponse.class));

        categoryService.create(request);

        verify(categoryRepository).save(argThat(c -> c.getName().equals("Technology")));
    }

    @Test
    void create_shouldThrowConflict_whenNameAlreadyTaken() {
        when(categoryRepository.existsByName("Technology")).thenReturn(true);

        assertThatThrownBy(() -> categoryService.create(new CategoryRequest("Technology", "desc")))
                .isInstanceOf(CategoryAlreadyExistsException.class);

        verify(categoryRepository, never()).save(any());
    }

    // ── update ───────────────────────────────────────────────

    @Test
    void update_shouldUpdateCategory_whenExists() {
        Category existing = new Category();
        existing.setName("Old Name");

        when(categoryRepository.findById(1)).thenReturn(Optional.of(existing));
        when(categoryRepository.existsByName("New Name")).thenReturn(false);
        when(categoryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(categoryMapper.toResponse(any())).thenReturn(mock(CategoryResponse.class));

        categoryService.update(1, new CategoryRequest("New Name", "desc"));

        verify(categoryRepository).save(argThat(c -> c.getName().equals("New Name")));
    }

    @Test
    void update_shouldThrowNotFound_whenCategoryNotExists() {
        when(categoryRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.update(999, new CategoryRequest("Name", "desc")))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void update_shouldThrowConflict_whenNewNameAlreadyTaken() {
        Category existing = new Category();
        existing.setName("Old Name");

        when(categoryRepository.findById(1)).thenReturn(Optional.of(existing));
        when(categoryRepository.existsByName("Taken Name")).thenReturn(true);

        assertThatThrownBy(() -> categoryService.update(1, new CategoryRequest("Taken Name", "desc")))
                .isInstanceOf(CategoryAlreadyExistsException.class);
    }

    @Test
    void update_shouldSaveCategory_whenNameRemainsTheSame() {
        Category existing = new Category();
        existing.setName("Same Name");
        existing.setDescription("Old description");

        when(categoryRepository.findById(1)).thenReturn(Optional.of(existing));
        when(categoryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(categoryMapper.toResponse(any())).thenReturn(mock(CategoryResponse.class));

        categoryService.update(1, new CategoryRequest("Same Name", "New description"));

        verify(categoryRepository, never()).existsByName(anyString());
        verify(categoryRepository).save(argThat(c -> c.getDescription().equals("New description")));
    }

    // ── delete ───────────────────────────────────────────────

    @Test
    void delete_shouldDelete_whenExists() {
        Category category = new Category();

        when(categoryRepository.findById(1)).thenReturn(Optional.of(category));

        categoryService.delete(1);

        verify(categoryRepository).delete(category);
    }

    @Test
    void delete_shouldThrowNotFound_whenNotExists() {
        when(categoryRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.delete(999))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}