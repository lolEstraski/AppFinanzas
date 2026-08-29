package com.finanzas.app.expenses.infrastructure.web;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.finanzas.app.auth.application.CurrentUserResolver;
import com.finanzas.app.auth.domain.User;
import com.finanzas.app.expenses.application.PaymentMethodService;
import com.finanzas.app.expenses.application.dto.PaymentMethodRequest;
import com.finanzas.app.expenses.application.dto.PaymentMethodResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/payment-methods")
public class PaymentMethodController {

    private final PaymentMethodService paymentMethodService;
    private final CurrentUserResolver currentUserResolver;

    public PaymentMethodController(PaymentMethodService paymentMethodService, CurrentUserResolver currentUserResolver) {
        this.paymentMethodService = paymentMethodService;
        this.currentUserResolver = currentUserResolver;
    }

    @GetMapping
    public List<PaymentMethodResponse> list(Authentication authentication) {
        User user = currentUserResolver.resolve(authentication);
        return paymentMethodService.listVisiblePaymentMethods(user);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentMethodResponse create(Authentication authentication, @Valid @RequestBody PaymentMethodRequest request) {
        User user = currentUserResolver.resolve(authentication);
        return paymentMethodService.createPaymentMethod(user, request);
    }
}
