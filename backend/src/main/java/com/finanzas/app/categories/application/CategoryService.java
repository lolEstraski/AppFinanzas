package com.finanzas.app.categories.application;

import java.util.List;

import org.springframework.stereotype.Service;

import com.finanzas.app.auth.domain.User;
import com.finanzas.app.categories.application.dto.CategoryRequest;
import com.finanzas.app.categories.application.dto.CategoryResponse;
import com.finanzas.app.categories.domain.Category;
import com.finanzas.app.categories.domain.CategoryRepository;
import com.finanzas.app.common.domain.exception.BusinessRuleViolationException;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<CategoryResponse> listVisibleCategories(User user) {
        return categoryRepository.findVisibleTo(user.getId()).stream()
                .map(CategoryService::toResponse)
                .toList();
    }

    public CategoryResponse createCategory(User user, CategoryRequest request) {
        if (categoryRepository.existsVisibleToByNameIgnoreCase(user.getId(), request.name())) {
            throw new BusinessRuleViolationException("Category already exists: " + request.name());
        }

        Category category = new Category();
        category.setName(request.name());
        category.setIcon(request.icon());
        category.setColor(request.color());
        category.setDefault(false);
        category.setUser(user);

        return toResponse(categoryRepository.save(category));
    }

    private static CategoryResponse toResponse(Category category) {
        return new CategoryResponse(category.getId(), category.getName(), category.getIcon(),
                category.getColor(), category.isDefault());
    }
}
