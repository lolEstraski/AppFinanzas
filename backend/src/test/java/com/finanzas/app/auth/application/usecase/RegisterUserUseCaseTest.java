package com.finanzas.app.auth.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.finanzas.app.auth.application.dto.RegisterRequest;
import com.finanzas.app.auth.application.dto.UserResponse;
import com.finanzas.app.auth.domain.AuthProvider;
import com.finanzas.app.auth.domain.User;
import com.finanzas.app.auth.domain.UserRepository;
import com.finanzas.app.auth.domain.exception.EmailAlreadyInUseException;

@ExtendWith(MockitoExtension.class)
class RegisterUserUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private RegisterUserUseCase registerUserUseCase;

    @Test
    void registersNewUserWithEncodedPassword() {
        registerUserUseCase = new RegisterUserUseCase(userRepository, passwordEncoder);
        RegisterRequest request = new RegisterRequest("jane@example.com", "Secret123!", "Jane Doe");
        given(userRepository.existsByEmail("jane@example.com")).willReturn(false);
        given(passwordEncoder.encode("Secret123!")).willReturn("hashed-password");
        given(userRepository.save(any(User.class))).willAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        UserResponse response = registerUserUseCase.register(request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.email()).isEqualTo("jane@example.com");
        assertThat(response.fullName()).isEqualTo("Jane Doe");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getPasswordHash()).isEqualTo("hashed-password");
        assertThat(captor.getValue().getAuthProvider()).isEqualTo(AuthProvider.LOCAL);
    }

    @Test
    void throwsWhenEmailAlreadyRegistered() {
        registerUserUseCase = new RegisterUserUseCase(userRepository, passwordEncoder);
        given(userRepository.existsByEmail("jane@example.com")).willReturn(true);
        RegisterRequest request = new RegisterRequest("jane@example.com", "Secret123!", "Jane Doe");

        assertThatThrownBy(() -> registerUserUseCase.register(request))
                .isInstanceOf(EmailAlreadyInUseException.class);

        verify(userRepository, never()).save(any());
    }
}
