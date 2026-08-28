package com.finanzas.app.auth.application.usecase;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.finanzas.app.auth.application.EmailSender;
import com.finanzas.app.auth.application.TokenService;
import com.finanzas.app.auth.application.dto.ForgotPasswordRequest;
import com.finanzas.app.auth.domain.PasswordResetTokenRepository;
import com.finanzas.app.auth.domain.User;
import com.finanzas.app.auth.domain.UserRepository;

@ExtendWith(MockitoExtension.class)
class RequestPasswordResetUseCaseTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock
    private TokenService tokenService;
    @Mock
    private EmailSender emailSender;

    private RequestPasswordResetUseCase requestPasswordResetUseCase;

    @Test
    void sendsResetEmailWhenUserExists() {
        requestPasswordResetUseCase = new RequestPasswordResetUseCase(
                userRepository, passwordResetTokenRepository, tokenService, emailSender);
        User user = new User();
        user.setId(1L);
        user.setEmail("jane@example.com");
        given(userRepository.findByEmail("jane@example.com")).willReturn(Optional.of(user));
        given(tokenService.generateOpaqueToken()).willReturn("raw-reset-token");

        requestPasswordResetUseCase.requestReset(new ForgotPasswordRequest("jane@example.com"));

        verify(passwordResetTokenRepository).save(any());
        verify(emailSender).sendPasswordResetEmail(eq("jane@example.com"), eq("raw-reset-token"));
    }

    @Test
    void doesNothingObservableWhenUserDoesNotExist() {
        requestPasswordResetUseCase = new RequestPasswordResetUseCase(
                userRepository, passwordResetTokenRepository, tokenService, emailSender);
        given(userRepository.findByEmail("missing@example.com")).willReturn(Optional.empty());

        requestPasswordResetUseCase.requestReset(new ForgotPasswordRequest("missing@example.com"));

        verify(passwordResetTokenRepository, never()).save(any());
        verify(emailSender, never()).sendPasswordResetEmail(any(), any());
    }
}
