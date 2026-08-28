package com.finanzas.app.categories.domain;

import java.util.List;

public interface CategoryRepository {

    Category save(Category category);

    List<Category> findVisibleTo(Long userId);

    boolean existsVisibleToByNameIgnoreCase(Long userId, String name);
}
