package com.nutrivida.servicios.dto;

import com.nutrivida.servicios.model.Modalidad;

public record ServicioResponse(
        Long id,
        String codigo,
        String nombre,
        String duracion,
        Modalidad modalidad,
        int precio,
        int stock,
        String descripcion,
        Long idCategoria,
        String categoria) {
}
