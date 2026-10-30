package com.me.backend_springboot.tenant.exception;

public class InvalidTenantDataException extends RuntimeException {

    public InvalidTenantDataException(String message) {
        super(message);
    }

}
