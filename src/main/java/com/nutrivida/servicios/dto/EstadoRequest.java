package com.nutrivida.servicios.dto;

import com.nutrivida.servicios.model.EstadoPedido;
import jakarta.validation.constraints.NotNull;

public record EstadoRequest(
        @NotNull(message = "El estado es obligatorio (PENDIENTE, CONFIRMADO, COMPLETADO o CANCELADO)")
        EstadoPedido estado) {
}
