package com.nutrivida.servicios.dto;

public record DetallePedidoResponse(
        Long idServicio,
        String codigo,
        String nombre,
        int cantidad,
        int precioUnitario,
        int subtotal) {
}
