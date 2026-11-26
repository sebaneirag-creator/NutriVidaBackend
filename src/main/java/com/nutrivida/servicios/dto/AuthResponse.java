package com.nutrivida.servicios.dto;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        long expiraEnSegundos,
        UsuarioResponse usuario) {
}
