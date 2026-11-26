package com.nutrivida.servicios.service;

import com.nutrivida.servicios.dto.EstadoCantidadResponse;
import com.nutrivida.servicios.dto.ResumenReporteResponse;
import com.nutrivida.servicios.dto.ServicioMasSolicitadoResponse;
import com.nutrivida.servicios.dto.VentaDiariaResponse;
import com.nutrivida.servicios.exception.ReglaNegocioException;
import com.nutrivida.servicios.model.EstadoPedido;
import com.nutrivida.servicios.repository.DetallePedidoRepository;
import com.nutrivida.servicios.repository.PedidoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
@RequiredArgsConstructor
public class ReporteService {

    private static final int TOP = 5;

    private final PedidoRepository pedidoRepository;
    private final DetallePedidoRepository detallePedidoRepository;

    /** periodo: semana (ultimos 7 dias), mes (ultimos 30 dias) o anio (ultimos 365 dias). */
    @Transactional(readOnly = true)
    public ResumenReporteResponse resumen(String periodo) {
        String clave = periodo == null ? "mes" : periodo.trim().toLowerCase();
        LocalDateTime hasta = LocalDateTime.now();
        LocalDateTime desde = switch (clave) {
            case "semana" -> hasta.minusDays(7);
            case "mes" -> hasta.minusDays(30);
            case "anio", "año" -> hasta.minusDays(365);
            default -> throw new ReglaNegocioException("Periodo inválido. Usa semana, mes o anio");
        };
        EstadoPedido excluido = EstadoPedido.CANCELADO;

        long totalPedidos = pedidoRepository.countByFechaGreaterThanEqualAndEstadoNot(desde, excluido);
        long montoTotal = nulo(pedidoRepository.sumarTotal(desde, excluido));
        long serviciosVendidos = nulo(detallePedidoRepository.sumarCantidad(desde, excluido));

        List<ServicioMasSolicitadoResponse> masSolicitados = detallePedidoRepository
                .masSolicitados(desde, excluido, PageRequest.of(0, TOP)).stream()
                .map(f -> new ServicioMasSolicitadoResponse(
                        (String) f[0], ((Number) f[1]).longValue(), ((Number) f[2]).longValue()))
                .toList();

        List<EstadoCantidadResponse> porEstado = pedidoRepository.contarPorEstado(desde).stream()
                .map(f -> new EstadoCantidadResponse(String.valueOf(f[0]), ((Number) f[1]).longValue()))
                .toList();

        return new ResumenReporteResponse(clave, desde, hasta, totalPedidos, montoTotal,
                serviciosVendidos, masSolicitados, porEstado, ventasPorDia(desde, excluido));
    }

    private List<VentaDiariaResponse> ventasPorDia(LocalDateTime desde, EstadoPedido excluido) {
        Map<LocalDate, long[]> dias = new TreeMap<>(); // [pedidos, monto]
        for (Object[] fila : pedidoRepository.fechasYTotales(desde, excluido)) {
            LocalDate dia = ((LocalDateTime) fila[0]).toLocalDate();
            long[] acumulado = dias.computeIfAbsent(dia, d -> new long[2]);
            acumulado[0]++;
            acumulado[1] += ((Number) fila[1]).longValue();
        }
        List<VentaDiariaResponse> resultado = new ArrayList<>();
        dias.forEach((dia, v) -> resultado.add(new VentaDiariaResponse(dia, v[0], v[1])));
        return resultado;
    }

    private long nulo(Long valor) {
        return valor == null ? 0 : valor;
    }
}
