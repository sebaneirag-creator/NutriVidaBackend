package com.nutrivida.servicios.exception;

/** Se traduce a HTTP 423 en GlobalExceptionHandler. */
public class CuentaBloqueadaException extends RuntimeException {

    public CuentaBloqueadaException(String mensaje) {
        super(mensaje);
    }
}
