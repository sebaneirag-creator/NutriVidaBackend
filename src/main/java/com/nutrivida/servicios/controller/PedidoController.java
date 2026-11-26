package com.nutrivida.servicios.controller;

import com.nutrivida.servicios.dto.EstadoRequest;
import com.nutrivida.servicios.dto.PedidoRequest;
import com.nutrivida.servicios.dto.PedidoResponse;
import com.nutrivida.servicios.model.EstadoPedido;
import com.nutrivida.servicios.security.UsuarioAutenticado;
import com.nutrivida.servicios.service.PedidoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
@Tag(name = "Pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    /** El pedido queda asociado al usuario del token (nunca a un id enviado por el cliente). */
    @PostMapping
    public ResponseEntity<PedidoResponse> crear(@Valid @RequestBody PedidoRequest req,
                                                @AuthenticationPrincipal UsuarioAutenticado actual) {
        PedidoResponse creado = pedidoService.crear(actual.id(), req);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(creado.id()).toUri();
        return ResponseEntity.created(ubicacion).body(creado);
    }

    /** Historial del usuario autenticado. */
    @GetMapping("/mis-pedidos")
    public List<PedidoResponse> misPedidos(@AuthenticationPrincipal UsuarioAutenticado actual) {
        return pedidoService.misPedidos(actual.id());
    }

    @GetMapping("/{id}")
    public PedidoResponse obtener(@PathVariable Long id, @AuthenticationPrincipal UsuarioAutenticado actual) {
        return pedidoService.obtener(id, actual);
    }

    @PatchMapping("/{id}/cancelar")
    public PedidoResponse cancelar(@PathVariable Long id, @AuthenticationPrincipal UsuarioAutenticado actual) {
        return pedidoService.cancelar(id, actual);
    }

    // ----- Solo ADMINISTRADOR y VENDEDOR (definido en SecurityConfig) -----

    /** Todos los pedidos; filtro opcional ?estado=PENDIENTE */
    @GetMapping
    public List<PedidoResponse> listarTodos(@RequestParam(required = false) EstadoPedido estado) {
        return pedidoService.listarTodos(estado);
    }

    @PatchMapping("/{id}/estado")
    public PedidoResponse cambiarEstado(@PathVariable Long id, @Valid @RequestBody EstadoRequest req) {
        return pedidoService.cambiarEstado(id, req.estado());
    }
}
