package com.finanzas.app.expenses.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.finanzas.app.expenses.domain.PaymentMethod;
import com.finanzas.app.expenses.domain.PaymentMethodRepository;

@Component
public class PaymentMethodRepositoryAdapter implements PaymentMethodRepository {

    private final PaymentMethodJpaRepository jpaRepository;

    public PaymentMethodRepositoryAdapter(PaymentMethodJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public PaymentMethod save(PaymentMethod paymentMethod) {
        return jpaRepository.save(paymentMethod);
    }

    @Override
    public List<PaymentMethod> findVisibleTo(Long userId) {
        return jpaRepository.findVisibleTo(userId);
    }

    @Override
    public Optional<PaymentMethod> findVisibleToById(Long userId, Long paymentMethodId) {
        return jpaRepository.findVisibleToById(userId, paymentMethodId);
    }

    @Override
    public boolean existsVisibleToByNameIgnoreCase(Long userId, String name) {
        return jpaRepository.existsVisibleToByNameIgnoreCase(userId, name);
    }
}
