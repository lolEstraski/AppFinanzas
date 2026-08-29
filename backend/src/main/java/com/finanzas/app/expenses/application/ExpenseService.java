package com.finanzas.app.expenses.application;

import java.util.List;

import org.springframework.stereotype.Service;

import com.finanzas.app.auth.domain.User;
import com.finanzas.app.categories.domain.Category;
import com.finanzas.app.categories.domain.CategoryRepository;
import com.finanzas.app.common.domain.exception.ResourceNotFoundException;
import com.finanzas.app.expenses.application.dto.ExpenseRequest;
import com.finanzas.app.expenses.application.dto.ExpenseResponse;
import com.finanzas.app.expenses.domain.Expense;
import com.finanzas.app.expenses.domain.ExpenseRepository;
import com.finanzas.app.expenses.domain.PaymentMethod;
import com.finanzas.app.expenses.domain.PaymentMethodRepository;

@Service
public class ExpenseService {

    private static final String DEFAULT_CURRENCY = "USD";

    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;
    private final PaymentMethodRepository paymentMethodRepository;

    public ExpenseService(ExpenseRepository expenseRepository,
                           CategoryRepository categoryRepository,
                           PaymentMethodRepository paymentMethodRepository) {
        this.expenseRepository = expenseRepository;
        this.categoryRepository = categoryRepository;
        this.paymentMethodRepository = paymentMethodRepository;
    }

    public ExpenseResponse createExpense(User user, ExpenseRequest request) {
        Category category = categoryRepository.findVisibleToById(user.getId(), request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + request.categoryId()));
        PaymentMethod paymentMethod = paymentMethodRepository.findVisibleToById(user.getId(), request.paymentMethodId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment method not found: " + request.paymentMethodId()));

        Expense expense = new Expense();
        expense.setUser(user);
        expense.setCategory(category);
        expense.setPaymentMethod(paymentMethod);
        expense.setAmount(request.amount());
        expense.setCurrency(request.currency() == null || request.currency().isBlank()
                ? DEFAULT_CURRENCY : request.currency());
        expense.setDescription(request.description());
        expense.setExpenseDate(request.expenseDate());

        return toResponse(expenseRepository.save(expense));
    }

    public List<ExpenseResponse> listExpenses(User user) {
        return expenseRepository.findByUserOrderByExpenseDateDesc(user.getId()).stream()
                .map(ExpenseService::toResponse)
                .toList();
    }

    private static ExpenseResponse toResponse(Expense expense) {
        return new ExpenseResponse(
                expense.getId(),
                expense.getCategory().getId(),
                expense.getCategory().getName(),
                expense.getPaymentMethod().getId(),
                expense.getPaymentMethod().getName(),
                expense.getAmount(),
                expense.getCurrency(),
                expense.getDescription(),
                expense.getExpenseDate());
    }
}
