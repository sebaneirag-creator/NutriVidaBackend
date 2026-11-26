package com.nutrivida.servicios.dto;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record RegistroRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 50, message = "El nombre no puede superar los 50 caracteres")
        String nombre,

        @NotBlank(message = "El apellido es obligatorio")
        @Size(max = 50, message = "El apellido no puede superar los 50 caracteres")
        String apellido,

        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo no tiene un formato válido")
        @Size(max = 100, message = "El correo no puede superar los 100 caracteres")
        @Pattern(regexp = "(?i)^[^@\\s]+@duocuc\\.cl$", message = "El correo debe ser del dominio @duocuc.cl")
        String correo,

        @NotBlank(message = "La contraseña es obligatoria")
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,60}$",
                message = "La contraseña debe tener 8 caracteres, una mayúscula, una minúscula y un número")
        String contrasena,

        @Past(message = "La fecha de nacimiento debe ser anterior a hoy")
        @NotNull
        LocalDate fechaNacimiento,

        @Size(max = 20, message = "El género no puede superar los 20 caracteres")
        @NotBlank
        String genero,

        @Size(max = 60, message = "La región no puede superar los 60 caracteres")
        @NotBlank
        String region,

        @Size(max = 150, message = "La dirección no puede superar los 150 caracteres")
        @NotBlank
        String direccion) {
}
