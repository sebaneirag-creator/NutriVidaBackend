package com.nutrivida.servicios.security;

/** Principal que queda en el SecurityContext tras validar el JWT. */
public record UsuarioAutenticado(Long id, String correo, String rol) {
}
