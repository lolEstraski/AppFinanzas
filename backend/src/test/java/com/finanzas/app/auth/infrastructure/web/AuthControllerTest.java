package com.finanzas.app.auth.infrastructure.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finanzas.app.auth.application.dto.AuthResponse;
import com.finanzas.app.auth.application.dto.ForgotPasswordRequest;
import com.finanzas.app.auth.application.dto.LoginRequest;
import com.finanzas.app.auth.application.dto.RefreshRequest;
import com.finanzas.app.auth.application.dto.RegisterRequest;
import com.finanzas.app.auth.application.dto.ResetPasswordRequest;
import com.finanzas.app.auth.application.dto.UserResponse;
import com.finanzas.app.auth.application.usecase.AuthenticateUserUseCase;
import com.finanzas.app.auth.application.usecase.RefreshAccessTokenUseCase;
import com.finanzas.app.auth.application.usecase.RegisterUserUseCase;
import com.finanzas.app.auth.application.usecase.RequestPasswordResetUseCase;
import com.finanzas.app.auth.application.usecase.ResetPasswordUseCase;
import com.finanzas.app.auth.domain.exception.EmailAlreadyInUseException;
import com.finanzas.app.auth.domain.exception.InvalidCredentialsException;
import com.finanzas.app.auth.domain.exception.InvalidOrExpiredTokenException;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RegisterUserUseCase registerUserUseCase;
    @MockitoBean
    private AuthenticateUserUseCase authenticateUserUseCase;
    @MockitoBean
    private RefreshAccessTokenUseCase refreshAccessTokenUseCase;
    @MockitoBean
    private RequestPasswordResetUseCase requestPasswordResetUseCase;
    @MockitoBean
    private ResetPasswordUseCase resetPasswordUseCase;

    @Test
    void registerReturnsCreatedUser() throws Exception {
        given(registerUserUseCase.register(any()))
                .willReturn(new UserResponse(1L, "jane@example.com", "Jane Doe", "USER"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new RegisterRequest("jane@example.com", "Secret123!", "Jane Doe"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("jane@example.com"));
    }

    @Test
    void registerReturnsBadRequestForInvalidPayload() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new RegisterRequest("not-an-email", "short", ""))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerReturnsConflictWhenEmailAlreadyUsed() throws Exception {
        given(registerUserUseCase.register(any())).willThrow(new EmailAlreadyInUseException("jane@example.com"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new RegisterRequest("jane@example.com", "Secret123!", "Jane Doe"))))
                .andExpect(status().isConflict());
    }

    @Test
    void loginReturnsTokens() throws Exception {
        given(authenticateUserUseCase.authenticate(any())).willReturn(new AuthResponse("access", "refresh", 900L));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LoginRequest("jane@example.com", "Secret123!"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access"));
    }

    @Test
    void loginReturnsUnauthorizedForInvalidCredentials() throws Exception {
        given(authenticateUserUseCase.authenticate(any())).willThrow(new InvalidCredentialsException());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LoginRequest("jane@example.com", "wrong-password"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refreshReturnsNewTokens() throws Exception {
        given(refreshAccessTokenUseCase.refresh(any()))
                .willReturn(new AuthResponse("new-access", "new-refresh", 900L));

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RefreshRequest("raw-refresh-token"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access"));
    }

    @Test
    void forgotPasswordAlwaysReturnsOk() throws Exception {
        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ForgotPasswordRequest("jane@example.com"))))
                .andExpect(status().isOk());

        verify(requestPasswordResetUseCase).requestReset(any());
    }

    @Test
    void resetPasswordReturnsBadRequestForInvalidToken() throws Exception {
        doThrow(new InvalidOrExpiredTokenException("Invalid reset token"))
                .when(resetPasswordUseCase).resetPassword(any());

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ResetPasswordRequest("bad-token", "NewSecret123!"))))
                .andExpect(status().isBadRequest());
    }
}
