package com.finanzas.app.expenses.application.dto;

import com.finanzas.app.expenses.domain.PaymentMethodType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PaymentMethodRequest(
        @NotBlank String name,
        @NotNull PaymentMethodType type
) {
}
