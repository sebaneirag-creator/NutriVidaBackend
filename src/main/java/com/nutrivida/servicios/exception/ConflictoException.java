package com.nutrivida.servicios.exception;

/** Se traduce a HTTP 409 en GlobalExceptionHandler. */
public class ConflictoException extends RuntimeException {

    public ConflictoException(String mensaje) {
        super(mensaje);
    }
}
