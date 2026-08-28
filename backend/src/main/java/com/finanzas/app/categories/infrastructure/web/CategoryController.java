package com.finanzas.app.categories.infrastructure.web;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.finanzas.app.auth.application.CurrentUserResolver;
import com.finanzas.app.auth.domain.User;
import com.finanzas.app.categories.application.CategoryService;
import com.finanzas.app.categories.application.dto.CategoryRequest;
import com.finanzas.app.categories.application.dto.CategoryResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;
    private final CurrentUserResolver currentUserResolver;

    public CategoryController(CategoryService categoryService, CurrentUserResolver currentUserResolver) {
        this.categoryService = categoryService;
        this.currentUserResolver = currentUserResolver;
    }

    @GetMapping
    public List<CategoryResponse> list(Authentication authentication) {
        User user = currentUserResolver.resolve(authentication);
        return categoryService.listVisibleCategories(user);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponse create(Authentication authentication, @Valid @RequestBody CategoryRequest request) {
        User user = currentUserResolver.resolve(authentication);
        return categoryService.createCategory(user, request);
    }
}
