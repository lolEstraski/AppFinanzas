package com.finanzas.app.categories.infrastructure.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finanzas.app.auth.application.CurrentUserResolver;
import com.finanzas.app.auth.domain.User;
import com.finanzas.app.categories.application.CategoryService;
import com.finanzas.app.categories.application.dto.CategoryRequest;
import com.finanzas.app.categories.application.dto.CategoryResponse;
import com.finanzas.app.common.domain.exception.BusinessRuleViolationException;

@WebMvcTest(CategoryController.class)
@AutoConfigureMockMvc(addFilters = false)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CategoryService categoryService;

    @MockitoBean
    private CurrentUserResolver currentUserResolver;

    private User authenticatedUser() {
        User user = new User();
        user.setId(1L);
        user.setEmail("jane@example.com");
        return user;
    }

    @Test
    void listsVisibleCategories() throws Exception {
        given(currentUserResolver.resolve(any())).willReturn(authenticatedUser());
        given(categoryService.listVisibleCategories(any()))
                .willReturn(List.of(new CategoryResponse(1L, "Comida", null, null, true)));

        mockMvc.perform(get("/api/categories").with(user("jane@example.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Comida"));
    }

    @Test
    void createsCategory() throws Exception {
        given(currentUserResolver.resolve(any())).willReturn(authenticatedUser());
        given(categoryService.createCategory(any(), any()))
                .willReturn(new CategoryResponse(10L, "Mascotas", "pet", "#FFAA00", false));

        mockMvc.perform(post("/api/categories")
                        .with(user("jane@example.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CategoryRequest("Mascotas", "pet", "#FFAA00"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Mascotas"));
    }

    @Test
    void createReturnsBadRequestWhenNameBlank() throws Exception {
        mockMvc.perform(post("/api/categories")
                        .with(user("jane@example.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CategoryRequest("", null, null))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createReturnsConflictWhenDuplicateName() throws Exception {
        given(currentUserResolver.resolve(any())).willReturn(authenticatedUser());
        given(categoryService.createCategory(any(), any()))
                .willThrow(new BusinessRuleViolationException("Category already exists: Comida"));

        mockMvc.perform(post("/api/categories")
                        .with(user("jane@example.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CategoryRequest("Comida", null, null))))
                .andExpect(status().isConflict());
    }
}
