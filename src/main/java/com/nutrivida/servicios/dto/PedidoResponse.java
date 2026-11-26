package com.nutrivida.servicios.dto;

import com.nutrivida.servicios.model.EstadoPedido;

import java.time.LocalDateTime;
import java.util.List;

public record PedidoResponse(
        Long id,
        Long idUsuario,
        String cliente,
        LocalDateTime fecha,
        EstadoPedido estado,
        int total,
        List<DetallePedidoResponse> detalles) {
}
