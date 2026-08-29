package com.finanzas.app.expenses.infrastructure.persistence;

import java.util.List;

import org.springframework.stereotype.Component;

import com.finanzas.app.expenses.domain.Expense;
import com.finanzas.app.expenses.domain.ExpenseRepository;

@Component
public class ExpenseRepositoryAdapter implements ExpenseRepository {

    private final ExpenseJpaRepository jpaRepository;

    public ExpenseRepositoryAdapter(ExpenseJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Expense save(Expense expense) {
        return jpaRepository.save(expense);
    }

    @Override
    public List<Expense> findByUserOrderByExpenseDateDesc(Long userId) {
        return jpaRepository.findByUserOrderByExpenseDateDesc(userId);
    }
}
