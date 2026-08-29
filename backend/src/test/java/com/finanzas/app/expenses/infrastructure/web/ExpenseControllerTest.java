package com.finanzas.app.expenses.infrastructure.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
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
import com.finanzas.app.common.domain.exception.ResourceNotFoundException;
import com.finanzas.app.expenses.application.ExpenseService;
import com.finanzas.app.expenses.application.dto.ExpenseRequest;
import com.finanzas.app.expenses.application.dto.ExpenseResponse;

@WebMvcTest(ExpenseController.class)
@AutoConfigureMockMvc(addFilters = false)
class ExpenseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ExpenseService expenseService;

    @MockitoBean
    private CurrentUserResolver currentUserResolver;

    private User authenticatedUser() {
        User user = new User();
        user.setId(1L);
        user.setEmail("jane@example.com");
        return user;
    }

    @Test
    void listsExpenses() throws Exception {
        given(currentUserResolver.resolve(any())).willReturn(authenticatedUser());
        given(expenseService.listExpenses(any())).willReturn(List.of(
                new ExpenseResponse(1L, 2L, "Comida", 3L, "Efectivo", new BigDecimal("25.50"), "USD",
                        "Almuerzo", LocalDate.of(2026, 8, 28))));

        mockMvc.perform(get("/api/expenses").with(user("jane@example.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].categoryName").value("Comida"));
    }

    @Test
    void createsExpense() throws Exception {
        given(currentUserResolver.resolve(any())).willReturn(authenticatedUser());
        given(expenseService.createExpense(any(), any())).willReturn(
                new ExpenseResponse(1L, 2L, "Comida", 3L, "Efectivo", new BigDecimal("25.50"), "USD",
                        "Almuerzo", LocalDate.of(2026, 8, 28)));

        ExpenseRequest request = new ExpenseRequest(2L, 3L, new BigDecimal("25.50"), "USD", "Almuerzo",
                LocalDate.of(2026, 8, 28));

        mockMvc.perform(post("/api/expenses")
                        .with(user("jane@example.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.categoryName").value("Comida"));
    }

    @Test
    void createReturnsBadRequestWhenAmountMissing() throws Exception {
        String invalidJson = "{\"categoryId\":2,\"paymentMethodId\":3,\"expenseDate\":\"2026-08-28\"}";

        mockMvc.perform(post("/api/expenses")
                        .with(user("jane@example.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createReturnsNotFoundWhenCategoryDoesNotExist() throws Exception {
        given(currentUserResolver.resolve(any())).willReturn(authenticatedUser());
        given(expenseService.createExpense(any(), any()))
                .willThrow(new ResourceNotFoundException("Category not found: 999"));

        ExpenseRequest request = new ExpenseRequest(999L, 3L, new BigDecimal("10.00"), "USD", null,
                LocalDate.of(2026, 8, 28));

        mockMvc.perform(post("/api/expenses")
                        .with(user("jane@example.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }
}
