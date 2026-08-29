package com.finanzas.app.expenses.domain;

import java.util.List;
import java.util.Optional;

public interface PaymentMethodRepository {

    PaymentMethod save(PaymentMethod paymentMethod);

    List<PaymentMethod> findVisibleTo(Long userId);

    Optional<PaymentMethod> findVisibleToById(Long userId, Long paymentMethodId);

    boolean existsVisibleToByNameIgnoreCase(Long userId, String name);
}
