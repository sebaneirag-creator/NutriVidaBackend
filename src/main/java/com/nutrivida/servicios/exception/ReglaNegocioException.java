package com.nutrivida.servicios.exception;

/** Se traduce a HTTP 400 en GlobalExceptionHandler. */
public class ReglaNegocioException extends RuntimeException {

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
