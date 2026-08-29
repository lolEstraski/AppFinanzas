package com.finanzas.app.categories.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.finanzas.app.categories.domain.Category;
import com.finanzas.app.categories.domain.CategoryRepository;

@Component
public class CategoryRepositoryAdapter implements CategoryRepository {

    private final CategoryJpaRepository jpaRepository;

    public CategoryRepositoryAdapter(CategoryJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Category save(Category category) {
        return jpaRepository.save(category);
    }

    @Override
    public List<Category> findVisibleTo(Long userId) {
        return jpaRepository.findVisibleTo(userId);
    }

    @Override
    public Optional<Category> findVisibleToById(Long userId, Long categoryId) {
        return jpaRepository.findVisibleToById(userId, categoryId);
    }

    @Override
    public boolean existsVisibleToByNameIgnoreCase(Long userId, String name) {
        return jpaRepository.existsVisibleToByNameIgnoreCase(userId, name);
    }
}
