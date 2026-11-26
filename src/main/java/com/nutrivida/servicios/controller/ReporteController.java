package com.nutrivida.servicios.controller;

import com.nutrivida.servicios.dto.ResumenReporteResponse;
import com.nutrivida.servicios.service.ReporteService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
@Tag(name = "Reportes")
public class ReporteController {

    private final ReporteService reporteService;

    /** ?periodo=semana | mes | anio (por defecto: mes). Solo ADMINISTRADOR y VENDEDOR. */
    @GetMapping("/resumen")
    public ResumenReporteResponse resumen(@RequestParam(defaultValue = "mes") String periodo) {
        return reporteService.resumen(periodo);
    }
}
