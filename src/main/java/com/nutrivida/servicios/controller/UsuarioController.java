package com.nutrivida.servicios.controller;

import com.nutrivida.servicios.dto.ActivoRequest;
import com.nutrivida.servicios.dto.PerfilRequest;
import com.nutrivida.servicios.dto.UsuarioRequest;
import com.nutrivida.servicios.dto.UsuarioResponse;
import com.nutrivida.servicios.security.UsuarioAutenticado;
import com.nutrivida.servicios.service.UsuarioService;
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
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@Tag(name = "Usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    // ----- Perfil propio (cualquier usuario autenticado) -----

    @GetMapping("/me")
    public UsuarioResponse miPerfil(@AuthenticationPrincipal UsuarioAutenticado actual) {
        return usuarioService.miPerfil(actual.id());
    }

    @PutMapping("/me")
    public UsuarioResponse actualizarMiPerfil(@AuthenticationPrincipal UsuarioAutenticado actual,
                                              @Valid @RequestBody PerfilRequest req) {
        return usuarioService.actualizarMiPerfil(actual.id(), req);
    }

    // ----- Administración (solo ADMINISTRADOR, definido en SecurityConfig) -----

    @GetMapping
    public List<UsuarioResponse> listar() {
        return usuarioService.listar();
    }

    @GetMapping("/{id}")
    public UsuarioResponse obtener(@PathVariable Long id) {
        return usuarioService.obtener(id);
    }

    @PostMapping
    public ResponseEntity<UsuarioResponse> crear(@Valid @RequestBody UsuarioRequest req) {
        UsuarioResponse creado = usuarioService.crear(req);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(creado.id()).toUri();
        return ResponseEntity.created(ubicacion).body(creado);
    }

    @PutMapping("/{id}")
    public UsuarioResponse actualizar(@PathVariable Long id,
                                      @Valid @RequestBody UsuarioRequest req,
                                      @AuthenticationPrincipal UsuarioAutenticado actual) {
        return usuarioService.actualizar(id, req, actual.id());
    }

    @PatchMapping("/{id}/activo")
    public UsuarioResponse cambiarActivo(@PathVariable Long id,
                                         @Valid @RequestBody ActivoRequest req,
                                         @AuthenticationPrincipal UsuarioAutenticado actual) {
        return usuarioService.cambiarActivo(id, req.activo(), actual.id());
    }

    @PatchMapping("/{id}/desbloquear")
    public UsuarioResponse desbloquear(@PathVariable Long id) {
        return usuarioService.desbloquear(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id,
                                         @AuthenticationPrincipal UsuarioAutenticado actual) {
        usuarioService.eliminar(id, actual.id());
        return ResponseEntity.noContent().build();
    }
}
