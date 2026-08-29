package com.finanzas.app.expenses.infrastructure.web;

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
import com.finanzas.app.expenses.application.ExpenseService;
import com.finanzas.app.expenses.application.dto.ExpenseRequest;
import com.finanzas.app.expenses.application.dto.ExpenseResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;
    private final CurrentUserResolver currentUserResolver;

    public ExpenseController(ExpenseService expenseService, CurrentUserResolver currentUserResolver) {
        this.expenseService = expenseService;
        this.currentUserResolver = currentUserResolver;
    }

    @GetMapping
    public List<ExpenseResponse> list(Authentication authentication) {
        User user = currentUserResolver.resolve(authentication);
        return expenseService.listExpenses(user);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ExpenseResponse create(Authentication authentication, @Valid @RequestBody ExpenseRequest request) {
        User user = currentUserResolver.resolve(authentication);
        return expenseService.createExpense(user, request);
    }
}
