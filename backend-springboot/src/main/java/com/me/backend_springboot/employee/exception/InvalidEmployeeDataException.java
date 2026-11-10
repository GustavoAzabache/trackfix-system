package com.me.backend_springboot.employee.exception;

public class InvalidEmployeeDataException extends RuntimeException {

    public InvalidEmployeeDataException(String message) {
        super(message);
    }

}
