package com.nutrivida.servicios.dto;

import com.nutrivida.servicios.model.Modalidad;
import jakarta.validation.constraints.*;

public record ServicioRequest(
        @NotBlank(message = "El código es obligatorio")
        @Size(max = 5, message = "El código no puede superar los 5 caracteres")
        String codigo,

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 80, message = "El nombre no puede superar los 80 caracteres")
        String nombre,

        @NotBlank(message = "La duración es obligatoria")
        @Size(max = 20, message = "La duración no puede superar los 20 caracteres")
        String duracion,

        @NotNull(message = "La modalidad es obligatoria (PRESENCIAL, PRESENCIAL_GRUPO u ONLINE_VIDEO)")
        Modalidad modalidad,

        @Min(value = 0, message = "El precio no puede ser negativo")
        int precio,

        @Min(value = 0, message = "El stock no puede ser negativo")
        @Max(999)
        int stock,

        @NotBlank(message = "La descripción es obligatoria")
        @Size(max = 255, message = "La descripción no puede superar los 255 caracteres")
        String descripcion,

        @NotNull(message = "La categoría es obligatoria")
        Long idCategoria) {
}
