package com.finanzas.app.expenses.application.dto;

import com.finanzas.app.expenses.domain.PaymentMethodType;

public record PaymentMethodResponse(
        Long id,
        String name,
        PaymentMethodType type,
        boolean isDefault
) {
}
