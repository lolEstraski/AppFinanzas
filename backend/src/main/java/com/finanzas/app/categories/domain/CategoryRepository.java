package com.finanzas.app.categories.domain;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository {

    Category save(Category category);

    List<Category> findVisibleTo(Long userId);

    Optional<Category> findVisibleToById(Long userId, Long categoryId);

    boolean existsVisibleToByNameIgnoreCase(Long userId, String name);
}
