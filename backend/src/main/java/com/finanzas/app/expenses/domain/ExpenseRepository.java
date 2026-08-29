package com.finanzas.app.expenses.domain;

import java.util.List;

public interface ExpenseRepository {

    Expense save(Expense expense);

    List<Expense> findByUserOrderByExpenseDateDesc(Long userId);
}
