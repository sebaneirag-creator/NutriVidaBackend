package com.nutrivida.servicios.dto;

import jakarta.validation.constraints.NotNull;

public record ActivoRequest(
        @NotNull(message = "El campo activo es obligatorio")
        Boolean activo) {
}
