package com.finanzas.app.expenses.application;

import java.util.List;

import org.springframework.stereotype.Service;

import com.finanzas.app.auth.domain.User;
import com.finanzas.app.common.domain.exception.BusinessRuleViolationException;
import com.finanzas.app.expenses.application.dto.PaymentMethodRequest;
import com.finanzas.app.expenses.application.dto.PaymentMethodResponse;
import com.finanzas.app.expenses.domain.PaymentMethod;
import com.finanzas.app.expenses.domain.PaymentMethodRepository;

@Service
public class PaymentMethodService {

    private final PaymentMethodRepository paymentMethodRepository;

    public PaymentMethodService(PaymentMethodRepository paymentMethodRepository) {
        this.paymentMethodRepository = paymentMethodRepository;
    }

    public List<PaymentMethodResponse> listVisiblePaymentMethods(User user) {
        return paymentMethodRepository.findVisibleTo(user.getId()).stream()
                .map(PaymentMethodService::toResponse)
                .toList();
    }

    public PaymentMethodResponse createPaymentMethod(User user, PaymentMethodRequest request) {
        if (paymentMethodRepository.existsVisibleToByNameIgnoreCase(user.getId(), request.name())) {
            throw new BusinessRuleViolationException("Payment method already exists: " + request.name());
        }

        PaymentMethod paymentMethod = new PaymentMethod();
        paymentMethod.setName(request.name());
        paymentMethod.setType(request.type());
        paymentMethod.setDefault(false);
        paymentMethod.setUser(user);

        return toResponse(paymentMethodRepository.save(paymentMethod));
    }

    private static PaymentMethodResponse toResponse(PaymentMethod paymentMethod) {
        return new PaymentMethodResponse(paymentMethod.getId(), paymentMethod.getName(),
                paymentMethod.getType(), paymentMethod.isDefault());
    }
}
