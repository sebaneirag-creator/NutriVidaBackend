package com.nutrivida.servicios.dto;

import java.time.LocalDateTime;
import java.util.List;

/** Indicadores del dashboard. Los pedidos CANCELADO no cuentan como venta (salvo en porEstado). */
public record ResumenReporteResponse(
        String periodo,
        LocalDateTime desde,
        LocalDateTime hasta,
        long totalPedidos,
        long montoTotal,
        long serviciosVendidos,
        List<ServicioMasSolicitadoResponse> masSolicitados,
        List<EstadoCantidadResponse> porEstado,
        List<VentaDiariaResponse> ventasPorDia) {
}
