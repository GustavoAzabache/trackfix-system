package com.me.backend_springboot.shared.security.exception;

/**
 * Lanza cuando un token JWT no es utilizable: firma inválida, expirado,
 * malformado o con el claim "type" equivocado.
 */
public class InvalidJwtException extends RuntimeException {

    public InvalidJwtException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidJwtException(String message) {
        super(message);
    }

}
