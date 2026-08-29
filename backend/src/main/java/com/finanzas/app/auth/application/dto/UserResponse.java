package com.finanzas.app.auth.application.dto;

public record UserResponse(
        Long id,
        String email,
        String fullName,
        String role
) {
}
