package com.finanzas.app.expenses.infrastructure.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.finanzas.app.expenses.domain.Expense;

public interface ExpenseJpaRepository extends JpaRepository<Expense, Long> {

    @Query("select e from Expense e "
            + "join fetch e.category join fetch e.paymentMethod "
            + "where e.user.id = :userId "
            + "order by e.expenseDate desc, e.id desc")
    List<Expense> findByUserOrderByExpenseDateDesc(@Param("userId") Long userId);
}
