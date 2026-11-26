package com.nutrivida.servicios.controller;

import com.nutrivida.servicios.dto.ServicioRequest;
import com.nutrivida.servicios.dto.ServicioResponse;
import com.nutrivida.servicios.dto.StockRequest;
import com.nutrivida.servicios.service.ServicioService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/servicios")
@RequiredArgsConstructor
@Tag(name = "Servicios")
public class ServicioController {

    private final ServicioService servicioService;

    /** Filtros opcionales: ?categoria=1&q=plan */
    @GetMapping
    public List<ServicioResponse> listar(@RequestParam(required = false) Long categoria,
                                         @RequestParam(required = false) String q) {
        return servicioService.listar(categoria, q);
    }

    @GetMapping("/{id}")
    public ServicioResponse obtener(@PathVariable Long id) {
        return servicioService.obtener(id);
    }

    @PostMapping
    public ResponseEntity<ServicioResponse> crear(@Valid @RequestBody ServicioRequest req) {
        ServicioResponse creado = servicioService.crear(req);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(creado.id()).toUri();
        return ResponseEntity.created(ubicacion).body(creado);
    }

    @PatchMapping("/{id}/stock")
    public ServicioResponse actualizarStock(@PathVariable Long id, @Valid @RequestBody StockRequest req) {
        return servicioService.actualizarStock(id, req.stock());
    }

    @PutMapping("/{id}")
    public ServicioResponse actualizar(@PathVariable Long id, @Valid @RequestBody ServicioRequest req) {
        return servicioService.actualizar(id, req);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        servicioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
