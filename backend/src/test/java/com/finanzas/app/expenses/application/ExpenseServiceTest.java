package com.finanzas.app.expenses.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
import com.finanzas.app.expenses.domain.PaymentMethodType;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceTest {

    @Mock
    private ExpenseRepository expenseRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private PaymentMethodRepository paymentMethodRepository;

    private ExpenseService expenseService;

    private User user() {
        User user = new User();
        user.setId(1L);
        user.setEmail("jane@example.com");
        return user;
    }

    private Category category() {
        Category category = new Category();
        category.setId(2L);
        category.setName("Comida");
        return category;
    }

    private PaymentMethod paymentMethod() {
        PaymentMethod method = new PaymentMethod();
        method.setId(3L);
        method.setName("Efectivo");
        method.setType(PaymentMethodType.CASH);
        return method;
    }

    @Test
    void createsExpenseWithDefaultCurrencyWhenNoneProvided() {
        expenseService = new ExpenseService(expenseRepository, categoryRepository, paymentMethodRepository);
        given(categoryRepository.findVisibleToById(1L, 2L)).willReturn(Optional.of(category()));
        given(paymentMethodRepository.findVisibleToById(1L, 3L)).willReturn(Optional.of(paymentMethod()));
        given(expenseRepository.save(any(Expense.class))).willAnswer(invocation -> {
            Expense saved = invocation.getArgument(0);
            saved.setId(100L);
            return saved;
        });

        ExpenseRequest request = new ExpenseRequest(2L, 3L, new BigDecimal("25.50"), null, "Almuerzo",
                LocalDate.of(2026, 8, 28));

        ExpenseResponse response = expenseService.createExpense(user(), request);

        assertThat(response.id()).isEqualTo(100L);
        assertThat(response.categoryName()).isEqualTo("Comida");
        assertThat(response.paymentMethodName()).isEqualTo("Efectivo");
        assertThat(response.currency()).isEqualTo("USD");
        assertThat(response.amount()).isEqualByComparingTo("25.50");
    }

    @Test
    void createsExpenseWithExplicitCurrency() {
        expenseService = new ExpenseService(expenseRepository, categoryRepository, paymentMethodRepository);
        User currentUser = user();
        given(categoryRepository.findVisibleToById(1L, 2L)).willReturn(Optional.of(category()));
        given(paymentMethodRepository.findVisibleToById(1L, 3L)).willReturn(Optional.of(paymentMethod()));
        given(expenseRepository.save(any(Expense.class))).willAnswer(invocation -> invocation.getArgument(0));

        ExpenseRequest request = new ExpenseRequest(2L, 3L, new BigDecimal("10.00"), "COP", null,
                LocalDate.of(2026, 8, 28));

        ExpenseResponse response = expenseService.createExpense(currentUser, request);

        assertThat(response.currency()).isEqualTo("COP");

        ArgumentCaptor<Expense> captor = ArgumentCaptor.forClass(Expense.class);
        verify(expenseRepository).save(captor.capture());
        assertThat(captor.getValue().getUser()).isSameAs(currentUser);
    }

    @Test
    void throwsWhenCategoryNotVisibleToUser() {
        expenseService = new ExpenseService(expenseRepository, categoryRepository, paymentMethodRepository);
        given(categoryRepository.findVisibleToById(1L, 2L)).willReturn(Optional.empty());

        ExpenseRequest request = new ExpenseRequest(2L, 3L, BigDecimal.TEN, null, null, LocalDate.now());

        assertThatThrownBy(() -> expenseService.createExpense(user(), request))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(expenseRepository, never()).save(any());
    }

    @Test
    void throwsWhenPaymentMethodNotVisibleToUser() {
        expenseService = new ExpenseService(expenseRepository, categoryRepository, paymentMethodRepository);
        given(categoryRepository.findVisibleToById(1L, 2L)).willReturn(Optional.of(category()));
        given(paymentMethodRepository.findVisibleToById(1L, 3L)).willReturn(Optional.empty());

        ExpenseRequest request = new ExpenseRequest(2L, 3L, BigDecimal.TEN, null, null, LocalDate.now());

        assertThatThrownBy(() -> expenseService.createExpense(user(), request))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(expenseRepository, never()).save(any());
    }

    @Test
    void listsExpensesForUser() {
        expenseService = new ExpenseService(expenseRepository, categoryRepository, paymentMethodRepository);
        Expense expense = new Expense();
        expense.setId(1L);
        expense.setCategory(category());
        expense.setPaymentMethod(paymentMethod());
        expense.setAmount(new BigDecimal("5.00"));
        expense.setCurrency("USD");
        expense.setExpenseDate(LocalDate.of(2026, 8, 1));
        given(expenseRepository.findByUserOrderByExpenseDateDesc(1L)).willReturn(List.of(expense));

        List<ExpenseResponse> responses = expenseService.listExpenses(user());

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).categoryName()).isEqualTo("Comida");
    }
}
