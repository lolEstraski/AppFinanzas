package com.finanzas.app.categories.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.finanzas.app.categories.domain.Category;

public interface CategoryJpaRepository extends JpaRepository<Category, Long> {

    @Query("select c from Category c where c.user is null or c.user.id = :userId")
    List<Category> findVisibleTo(@Param("userId") Long userId);

    @Query("select c from Category c where (c.user is null or c.user.id = :userId) and c.id = :categoryId")
    Optional<Category> findVisibleToById(@Param("userId") Long userId, @Param("categoryId") Long categoryId);

    @Query("select count(c) > 0 from Category c "
            + "where (c.user is null or c.user.id = :userId) and lower(c.name) = lower(:name)")
    boolean existsVisibleToByNameIgnoreCase(@Param("userId") Long userId, @Param("name") String name);
}
