package com.me.backend_springboot.auth.exception;

public class InvalidAuthDataException extends RuntimeException {

    public InvalidAuthDataException(String message) {
        super(message);
    }

}
