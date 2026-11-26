package com.nutrivida.servicios.dto;

import com.nutrivida.servicios.model.Usuario;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Nunca incluye la contraseña. */
public record UsuarioResponse(
        Long id,
        String nombre,
        String apellido,
        String correo,
        LocalDate fechaNacimiento,
        String genero,
        String region,
        String direccion,
        boolean activo,
        boolean bloqueado,
        String rol) {

    public static UsuarioResponse desde(Usuario u) {
        boolean bloqueado = u.getBloqueadoHasta() != null && u.getBloqueadoHasta().isAfter(LocalDateTime.now());
        return new UsuarioResponse(u.getId(), u.getNombre(), u.getApellido(), u.getCorreo(),
                u.getFechaNacimiento(), u.getGenero(), u.getRegion(), u.getDireccion(),
                u.isActivo(), bloqueado, u.getRol().getNombre());
    }
}
