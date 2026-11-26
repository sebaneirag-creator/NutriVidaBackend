package com.nutrivida.servicios.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record PedidoItemRequest(
        @NotNull(message = "El servicio es obligatorio")
        Long idServicio,

        @Min(value = 1, message = "La cantidad mínima es 1")
        @Max(value = 99, message = "La cantidad máxima por servicio es 99")
        int cantidad) {
}
