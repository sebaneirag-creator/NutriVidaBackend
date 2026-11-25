package com.nutrivida.servicios.exception;

/** Se traduce a HTTP 404 en GlobalExceptionHandler. */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
