package com.finanzas.app.auth.application.dto;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        long expiresInSeconds
) {
}
