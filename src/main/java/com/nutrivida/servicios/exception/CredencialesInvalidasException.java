package com.nutrivida.servicios.exception;

/** Se traduce a HTTP 401 en GlobalExceptionHandler. */
public class CredencialesInvalidasException extends RuntimeException {

    public CredencialesInvalidasException(String mensaje) {
        super(mensaje);
    }
}
