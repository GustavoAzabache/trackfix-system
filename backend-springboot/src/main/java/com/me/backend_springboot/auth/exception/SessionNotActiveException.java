package com.me.backend_springboot.auth.exception;

public class SessionNotActiveException extends RuntimeException {

    public SessionNotActiveException() {
        super("La sesión ya no es válida. Inicia sesión de nuevo.");
    }

    public SessionNotActiveException(String message) {
        super(message);
    }

}
