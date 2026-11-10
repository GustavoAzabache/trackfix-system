package com.me.backend_springboot.auth.exception;

public class EmailAlreadyRegisteredException extends RuntimeException {

    public EmailAlreadyRegisteredException() {
        super("El email ya está registrado.");
    }

}
