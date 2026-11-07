package com.me.backend_springboot.auth.exception;

public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        // Mismo mensaje para email inexistente y contraseña incorrecta: no se revela cuál falló
        super("El correo o la contraseña son incorrectos.");
    }

}
