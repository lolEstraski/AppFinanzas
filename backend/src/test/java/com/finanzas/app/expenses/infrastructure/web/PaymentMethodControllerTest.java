package com.finanzas.app.expenses.infrastructure.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finanzas.app.auth.application.CurrentUserResolver;
import com.finanzas.app.auth.domain.User;
import com.finanzas.app.common.domain.exception.BusinessRuleViolationException;
import com.finanzas.app.expenses.application.PaymentMethodService;
import com.finanzas.app.expenses.application.dto.PaymentMethodRequest;
import com.finanzas.app.expenses.application.dto.PaymentMethodResponse;
import com.finanzas.app.expenses.domain.PaymentMethodType;

@WebMvcTest(PaymentMethodController.class)
@AutoConfigureMockMvc(addFilters = false)
class PaymentMethodControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PaymentMethodService paymentMethodService;

    @MockitoBean
    private CurrentUserResolver currentUserResolver;

    private User authenticatedUser() {
        User user = new User();
        user.setId(1L);
        user.setEmail("jane@example.com");
        return user;
    }

    @Test
    void listsVisiblePaymentMethods() throws Exception {
        given(currentUserResolver.resolve(any())).willReturn(authenticatedUser());
        given(paymentMethodService.listVisiblePaymentMethods(any()))
                .willReturn(List.of(new PaymentMethodResponse(1L, "Efectivo", PaymentMethodType.CASH, true)));

        mockMvc.perform(get("/api/payment-methods").with(user("jane@example.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Efectivo"));
    }

    @Test
    void createsPaymentMethod() throws Exception {
        given(currentUserResolver.resolve(any())).willReturn(authenticatedUser());
        given(paymentMethodService.createPaymentMethod(any(), any()))
                .willReturn(new PaymentMethodResponse(5L, "Nequi", PaymentMethodType.OTHER, false));

        mockMvc.perform(post("/api/payment-methods")
                        .with(user("jane@example.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new PaymentMethodRequest("Nequi", PaymentMethodType.OTHER))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Nequi"));
    }

    @Test
    void createReturnsBadRequestWhenNameBlank() throws Exception {
        mockMvc.perform(post("/api/payment-methods")
                        .with(user("jane@example.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new PaymentMethodRequest("", PaymentMethodType.CASH))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createReturnsConflictWhenDuplicateName() throws Exception {
        given(currentUserResolver.resolve(any())).willReturn(authenticatedUser());
        given(paymentMethodService.createPaymentMethod(any(), any()))
                .willThrow(new BusinessRuleViolationException("Payment method already exists: Efectivo"));

        mockMvc.perform(post("/api/payment-methods")
                        .with(user("jane@example.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new PaymentMethodRequest("Efectivo", PaymentMethodType.CASH))))
                .andExpect(status().isConflict());
    }
}
