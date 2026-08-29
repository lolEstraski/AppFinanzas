package com.finanzas.app.expenses.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseResponse(
        Long id,
        Long categoryId,
        String categoryName,
        Long paymentMethodId,
        String paymentMethodName,
        BigDecimal amount,
        String currency,
        String description,
        LocalDate expenseDate
) {
}
