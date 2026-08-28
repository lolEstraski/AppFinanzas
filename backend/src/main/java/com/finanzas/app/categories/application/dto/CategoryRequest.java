package com.finanzas.app.categories.application.dto;

import jakarta.validation.constraints.NotBlank;

public record CategoryRequest(
        @NotBlank String name,
        String icon,
        String color
) {
}
