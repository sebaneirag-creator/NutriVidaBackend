package com.nutrivida.servicios.service;

import com.nutrivida.servicios.exception.ReglaNegocioException;

import java.time.LocalDate;
import java.time.Period;

final class ReglasUsuario {

    private static final int EDAD_MINIMA = 14;

    private ReglasUsuario() {}

    static void exigirEdadMinima(LocalDate fechaNacimiento) {
        if (Period.between(fechaNacimiento, LocalDate.now()).getYears() < EDAD_MINIMA) {
            throw new ReglaNegocioException("El registro es solo para personas mayores a " + EDAD_MINIMA + " años");
        }
    }
}