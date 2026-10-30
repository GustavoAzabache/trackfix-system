package com.me.backend_springboot.tenant.exception;

public class InvalidPhoneException extends InvalidTenantDataException {

    public InvalidPhoneException(String message) {
        super(message);
    }

}
