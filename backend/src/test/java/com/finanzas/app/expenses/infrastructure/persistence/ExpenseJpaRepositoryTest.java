package com.finanzas.app.expenses.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.hibernate.Hibernate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import com.finanzas.app.auth.domain.AuthProvider;
import com.finanzas.app.auth.domain.Role;
import com.finanzas.app.auth.domain.User;
import com.finanzas.app.auth.infrastructure.persistence.UserJpaRepository;
import com.finanzas.app.categories.domain.Category;
import com.finanzas.app.categories.infrastructure.persistence.CategoryJpaRepository;
import com.finanzas.app.config.JpaAuditingConfig;
import com.finanzas.app.expenses.domain.Expense;
import com.finanzas.app.expenses.domain.PaymentMethod;
import com.finanzas.app.expenses.domain.PaymentMethodType;

import jakarta.persistence.EntityManager;

@DataJpaTest
@Import(JpaAuditingConfig.class)
class ExpenseJpaRepositoryTest {

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private CategoryJpaRepository categoryJpaRepository;

    @Autowired
    private PaymentMethodJpaRepository paymentMethodJpaRepository;

    @Autowired
    private ExpenseJpaRepository expenseJpaRepository;

    @Autowired
    private EntityManager entityManager;

    private User persistedUser(String email) {
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash("hashed-password");
        user.setFullName("Jane Doe");
        user.setAuthProvider(AuthProvider.LOCAL);
        user.setRole(Role.USER);
        return userJpaRepository.saveAndFlush(user);
    }

    private Category persistedCategory() {
        Category category = new Category();
        category.setName("Comida");
        category.setDefault(true);
        return categoryJpaRepository.saveAndFlush(category);
    }

    private PaymentMethod persistedPaymentMethod() {
        PaymentMethod method = new PaymentMethod();
        method.setName("Efectivo");
        method.setType(PaymentMethodType.CASH);
        method.setDefault(true);
        return paymentMethodJpaRepository.saveAndFlush(method);
    }

    private Expense expense(User user, Category category, PaymentMethod method, LocalDate date) {
        Expense expense = new Expense();
        expense.setUser(user);
        expense.setCategory(category);
        expense.setPaymentMethod(method);
        expense.setAmount(new BigDecimal("12.34"));
        expense.setCurrency("USD");
        expense.setExpenseDate(date);
        return expense;
    }

    @Test
    void listsExpensesForUserOrderedByDateDescendingWithCategoryAndPaymentMethodEagerlyLoaded() {
        User owner = persistedUser("owner@example.com");
        User other = persistedUser("other@example.com");
        Category category = persistedCategory();
        PaymentMethod method = persistedPaymentMethod();
        expenseJpaRepository.saveAndFlush(expense(owner, category, method, LocalDate.of(2026, 8, 1)));
        expenseJpaRepository.saveAndFlush(expense(owner, category, method, LocalDate.of(2026, 8, 15)));
        expenseJpaRepository.saveAndFlush(expense(other, category, method, LocalDate.of(2026, 8, 20)));
        entityManager.clear();

        var expenses = expenseJpaRepository.findByUserOrderByExpenseDateDesc(owner.getId());

        assertThat(expenses).hasSize(2);
        assertThat(expenses.get(0).getExpenseDate()).isEqualTo(LocalDate.of(2026, 8, 15));
        assertThat(expenses.get(1).getExpenseDate()).isEqualTo(LocalDate.of(2026, 8, 1));
        assertThat(Hibernate.isInitialized(expenses.get(0).getCategory())).isTrue();
        assertThat(Hibernate.isInitialized(expenses.get(0).getPaymentMethod())).isTrue();
        assertThat(expenses.get(0).getCategory().getName()).isEqualTo("Comida");
        assertThat(expenses.get(0).getPaymentMethod().getName()).isEqualTo("Efectivo");
    }
}
