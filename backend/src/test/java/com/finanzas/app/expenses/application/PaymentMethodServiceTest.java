package com.finanzas.app.expenses.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.finanzas.app.auth.domain.User;
import com.finanzas.app.common.domain.exception.BusinessRuleViolationException;
import com.finanzas.app.expenses.application.dto.PaymentMethodRequest;
import com.finanzas.app.expenses.application.dto.PaymentMethodResponse;
import com.finanzas.app.expenses.domain.PaymentMethod;
import com.finanzas.app.expenses.domain.PaymentMethodRepository;
import com.finanzas.app.expenses.domain.PaymentMethodType;

@ExtendWith(MockitoExtension.class)
class PaymentMethodServiceTest {

    @Mock
    private PaymentMethodRepository paymentMethodRepository;

    private PaymentMethodService paymentMethodService;

    private User user() {
        User user = new User();
        user.setId(1L);
        user.setEmail("jane@example.com");
        return user;
    }

    private PaymentMethod globalMethod(Long id, String name, PaymentMethodType type) {
        PaymentMethod method = new PaymentMethod();
        method.setId(id);
        method.setName(name);
        method.setType(type);
        method.setDefault(true);
        return method;
    }

    @Test
    void listsPaymentMethodsVisibleToUser() {
        paymentMethodService = new PaymentMethodService(paymentMethodRepository);
        given(paymentMethodRepository.findVisibleTo(1L))
                .willReturn(List.of(globalMethod(1L, "Efectivo", PaymentMethodType.CASH),
                        globalMethod(2L, "Tarjeta", PaymentMethodType.CARD)));

        List<PaymentMethodResponse> responses = paymentMethodService.listVisiblePaymentMethods(user());

        assertThat(responses).extracting(PaymentMethodResponse::name).containsExactly("Efectivo", "Tarjeta");
    }

    @Test
    void createsCustomPaymentMethodForUser() {
        paymentMethodService = new PaymentMethodService(paymentMethodRepository);
        User currentUser = user();
        given(paymentMethodRepository.existsVisibleToByNameIgnoreCase(1L, "Nequi")).willReturn(false);
        given(paymentMethodRepository.save(any(PaymentMethod.class))).willAnswer(invocation -> {
            PaymentMethod saved = invocation.getArgument(0);
            saved.setId(5L);
            return saved;
        });

        PaymentMethodResponse response = paymentMethodService.createPaymentMethod(
                currentUser, new PaymentMethodRequest("Nequi", PaymentMethodType.OTHER));

        assertThat(response.id()).isEqualTo(5L);
        assertThat(response.name()).isEqualTo("Nequi");
        assertThat(response.isDefault()).isFalse();

        ArgumentCaptor<PaymentMethod> captor = ArgumentCaptor.forClass(PaymentMethod.class);
        verify(paymentMethodRepository).save(captor.capture());
        assertThat(captor.getValue().getUser()).isSameAs(currentUser);
        assertThat(captor.getValue().isDefault()).isFalse();
    }

    @Test
    void rejectsDuplicatePaymentMethodNameForUser() {
        paymentMethodService = new PaymentMethodService(paymentMethodRepository);
        given(paymentMethodRepository.existsVisibleToByNameIgnoreCase(1L, "Efectivo")).willReturn(true);

        assertThatThrownBy(() -> paymentMethodService.createPaymentMethod(
                user(), new PaymentMethodRequest("Efectivo", PaymentMethodType.CASH)))
                .isInstanceOf(BusinessRuleViolationException.class);

        verify(paymentMethodRepository, never()).save(any());
    }
}
