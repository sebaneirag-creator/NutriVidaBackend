package com.nutrivida.servicios.dto;

import java.time.LocalDate;

public record VentaDiariaResponse(LocalDate fecha, long pedidos, long monto) {
}
