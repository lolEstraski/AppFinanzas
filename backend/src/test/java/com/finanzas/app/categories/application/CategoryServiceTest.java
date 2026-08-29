package com.finanzas.app.categories.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.finanzas.app.auth.domain.User;
import com.finanzas.app.categories.application.dto.CategoryRequest;
import com.finanzas.app.categories.application.dto.CategoryResponse;
import com.finanzas.app.categories.domain.Category;
import com.finanzas.app.categories.domain.CategoryRepository;
import com.finanzas.app.common.domain.exception.BusinessRuleViolationException;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    private CategoryService categoryService;

    private User user() {
        User user = new User();
        user.setId(1L);
        user.setEmail("jane@example.com");
        return user;
    }

    private Category globalCategory(Long id, String name) {
        Category category = new Category();
        category.setId(id);
        category.setName(name);
        category.setDefault(true);
        return category;
    }

    @Test
    void listsCategoriesVisibleToUser() {
        categoryService = new CategoryService(categoryRepository);
        given(categoryRepository.findVisibleTo(1L))
                .willReturn(List.of(globalCategory(1L, "Comida"), globalCategory(2L, "Gasolina")));

        List<CategoryResponse> responses = categoryService.listVisibleCategories(user());

        assertThat(responses).extracting(CategoryResponse::name).containsExactly("Comida", "Gasolina");
        assertThat(responses).allMatch(CategoryResponse::isDefault);
    }

    @Test
    void createsCustomCategoryForUser() {
        categoryService = new CategoryService(categoryRepository);
        User currentUser = user();
        given(categoryRepository.existsVisibleToByNameIgnoreCase(1L, "Mascotas")).willReturn(false);
        given(categoryRepository.save(any(Category.class))).willAnswer(invocation -> {
            Category saved = invocation.getArgument(0);
            saved.setId(10L);
            return saved;
        });

        CategoryResponse response = categoryService.createCategory(currentUser, new CategoryRequest("Mascotas", "pet", "#FFAA00"));

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.name()).isEqualTo("Mascotas");
        assertThat(response.isDefault()).isFalse();

        ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);
        verify(categoryRepository).save(captor.capture());
        assertThat(captor.getValue().getUser()).isSameAs(currentUser);
        assertThat(captor.getValue().isDefault()).isFalse();
    }

    @Test
    void rejectsDuplicateCategoryNameForUser() {
        categoryService = new CategoryService(categoryRepository);
        given(categoryRepository.existsVisibleToByNameIgnoreCase(1L, "Comida")).willReturn(true);

        assertThatThrownBy(() -> categoryService.createCategory(user(), new CategoryRequest("Comida", null, null)))
                .isInstanceOf(BusinessRuleViolationException.class);

        verify(categoryRepository, never()).save(any());
    }
}
