package com.finanzas.app.categories.application.dto;

public record CategoryResponse(
        Long id,
        String name,
        String icon,
        String color,
        boolean isDefault
) {
}
