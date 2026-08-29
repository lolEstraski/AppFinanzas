package com.finanzas.app.auth.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.finanzas.app.auth.application.TokenService;
import com.finanzas.app.auth.application.dto.AuthResponse;
import com.finanzas.app.auth.application.dto.LoginRequest;
import com.finanzas.app.auth.domain.RefreshTokenRepository;
import com.finanzas.app.auth.domain.User;
import com.finanzas.app.auth.domain.UserRepository;
import com.finanzas.app.auth.domain.exception.InvalidCredentialsException;
import com.finanzas.app.config.JwtProperties;

@ExtendWith(MockitoExtension.class)
class AuthenticateUserUseCaseTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private TokenService tokenService;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    private AuthenticateUserUseCase authenticateUserUseCase;

    @BeforeEach
    void setUp() {
        JwtProperties jwtProperties = new JwtProperties("test-secret", 900000L, 1209600000L);
        authenticateUserUseCase = new AuthenticateUserUseCase(
                userRepository, passwordEncoder, tokenService, refreshTokenRepository, jwtProperties);
    }

    private User localUser() {
        User user = new User();
        user.setId(1L);
        user.setEmail("jane@example.com");
        user.setPasswordHash("hashed-password");
        return user;
    }

    @Test
    void authenticatesUserWithValidCredentials() {
        User user = localUser();
        given(userRepository.findByEmail("jane@example.com")).willReturn(Optional.of(user));
        given(passwordEncoder.matches("Secret123!", "hashed-password")).willReturn(true);
        given(tokenService.generateAccessToken(user)).willReturn("access-token");
        given(tokenService.generateOpaqueToken()).willReturn("raw-refresh-token");

        AuthResponse response = authenticateUserUseCase.authenticate(new LoginRequest("jane@example.com", "Secret123!"));

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("raw-refresh-token");
        assertThat(response.expiresInSeconds()).isEqualTo(900L);
        verify(refreshTokenRepository).save(any());
    }

    @Test
    void throwsWhenUserNotFound() {
        given(userRepository.findByEmail("missing@example.com")).willReturn(Optional.empty());

        assertThatThrownBy(() -> authenticateUserUseCase.authenticate(new LoginRequest("missing@example.com", "whatever")))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void throwsWhenPasswordDoesNotMatch() {
        User user = localUser();
        given(userRepository.findByEmail("jane@example.com")).willReturn(Optional.of(user));
        given(passwordEncoder.matches("wrong-password", "hashed-password")).willReturn(false);

        assertThatThrownBy(() -> authenticateUserUseCase.authenticate(new LoginRequest("jane@example.com", "wrong-password")))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void throwsWhenUserHasNoLocalPassword() {
        User googleUser = new User();
        googleUser.setId(2L);
        googleUser.setEmail("jane@example.com");
        googleUser.setPasswordHash(null);
        given(userRepository.findByEmail("jane@example.com")).willReturn(Optional.of(googleUser));

        assertThatThrownBy(() -> authenticateUserUseCase.authenticate(new LoginRequest("jane@example.com", "anything")))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(refreshTokenRepository, never()).save(any());
    }
}
