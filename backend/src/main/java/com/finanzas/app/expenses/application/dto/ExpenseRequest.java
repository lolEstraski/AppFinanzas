package com.finanzas.app.expenses.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record ExpenseRequest(
        @NotNull Long categoryId,
        @NotNull Long paymentMethodId,
        @NotNull @DecimalMin(value = "0.01") BigDecimal amount,
        String currency,
        String description,
        @NotNull LocalDate expenseDate
) {
}
