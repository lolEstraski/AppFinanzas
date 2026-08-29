package com.finanzas.app.auth.infrastructure.web;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

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

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final RegisterUserUseCase registerUserUseCase;
    private final AuthenticateUserUseCase authenticateUserUseCase;
    private final RefreshAccessTokenUseCase refreshAccessTokenUseCase;
    private final RequestPasswordResetUseCase requestPasswordResetUseCase;
    private final ResetPasswordUseCase resetPasswordUseCase;

    public AuthController(RegisterUserUseCase registerUserUseCase,
                           AuthenticateUserUseCase authenticateUserUseCase,
                           RefreshAccessTokenUseCase refreshAccessTokenUseCase,
                           RequestPasswordResetUseCase requestPasswordResetUseCase,
                           ResetPasswordUseCase resetPasswordUseCase) {
        this.registerUserUseCase = registerUserUseCase;
        this.authenticateUserUseCase = authenticateUserUseCase;
        this.refreshAccessTokenUseCase = refreshAccessTokenUseCase;
        this.requestPasswordResetUseCase = requestPasswordResetUseCase;
        this.resetPasswordUseCase = resetPasswordUseCase;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody RegisterRequest request) {
        return registerUserUseCase.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authenticateUserUseCase.authenticate(request);
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(@Valid @RequestBody RefreshRequest request) {
        return refreshAccessTokenUseCase.refresh(request);
    }

    @PostMapping("/forgot-password")
    public void forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        requestPasswordResetUseCase.requestReset(request);
    }

    @PostMapping("/reset-password")
    public void resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        resetPasswordUseCase.resetPassword(request);
    }
}
