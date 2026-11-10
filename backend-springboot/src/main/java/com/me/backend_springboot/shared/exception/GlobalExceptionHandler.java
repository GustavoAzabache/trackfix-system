package com.me.backend_springboot.shared.exception;

import java.util.List;
import java.util.Locale;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.me.backend_springboot.auth.exception.EmailAlreadyRegisteredException;
import com.me.backend_springboot.auth.exception.InvalidAuthDataException;
import com.me.backend_springboot.auth.exception.InvalidCredentialsException;
import com.me.backend_springboot.auth.exception.SessionNotActiveException;
import com.me.backend_springboot.customer.exception.CustomerDataException;
import com.me.backend_springboot.employee.exception.InvalidEmployeeDataException;
import com.me.backend_springboot.shared.security.exception.InvalidJwtException;
import com.me.backend_springboot.tenant.exception.InvalidTenantDataException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Email duplicado detectado por el pre-check del registro
    @ExceptionHandler(EmailAlreadyRegisteredException.class)
    public ResponseEntity<ErrorResponse> handleEmailAlreadyRegistered(EmailAlreadyRegisteredException ex) {
        return error(HttpStatus.CONFLICT, ex.getMessage());
    }

    // Red de seguridad ante carreras de concurrencia (email, código de taller,
    // nombre, teléfono o RUC únicos que llegaron a la base de datos)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        if (isDuplicateKey(ex)) {
            return error(HttpStatus.CONFLICT, "Ya existe un registro con un valor único duplicado (email, nombre, teléfono, RUC o código).");
        }

        return error(HttpStatus.INTERNAL_SERVER_ERROR, "Violación de integridad de datos.");
    }

    // Validaciones de negocio del taller (incluye RUC, teléfono y código)
    @ExceptionHandler(InvalidTenantDataException.class)
    public ResponseEntity<ErrorResponse> handleInvalidTenantData(InvalidTenantDataException ex) {
        return error(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler({
        InvalidAuthDataException.class,
        InvalidEmployeeDataException.class,
        CustomerDataException.class
    })
    public ResponseEntity<ErrorResponse> handleInvalidData(Exception ex) {
        return error(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // Login con email o contraseña incorrectos (mensaje único: no se revela cuál falló)
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(InvalidCredentialsException ex) {
        return error(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    // Sesión revocada o vencida usada en /refresh
    @ExceptionHandler(SessionNotActiveException.class)
    public ResponseEntity<ErrorResponse> handleSessionNotActive(SessionNotActiveException ex) {
        return error(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    // JWT inválido/expirado/tipo incorrecto llegando a un endpoint (el JwtFilter lo traga en requests protegidos)
    @ExceptionHandler(InvalidJwtException.class)
    public ResponseEntity<ErrorResponse> handleInvalidJwt(InvalidJwtException ex) {
        return error(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    // Errores de Bean Validation en los DTOs de petición (@Valid + @RequestBody): uno por campo
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        List<String> messages = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .toList();

        return error(HttpStatus.BAD_REQUEST, messages);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFound(NoResourceFoundException ex) {
        return error(HttpStatus.NOT_FOUND, "Recurso no encontrado.");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        return error(HttpStatus.METHOD_NOT_ALLOWED, "Método HTTP no soportado para este recurso.");
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex) {
        return error(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Tipo de contenido no soportado.");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleNotReadable(HttpMessageNotReadableException ex) {
        return error(HttpStatus.BAD_REQUEST, "Cuerpo de la petición inválido o mal formado.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "Error inesperado en el servidor.");
    }

    private static ResponseEntity<ErrorResponse> error(HttpStatus status, String message) {
        return ResponseEntity
            .status(status)
            .body(new ErrorResponse(status.value(), message));
    }

    private static ResponseEntity<ErrorResponse> error(HttpStatus status, List<String> messages) {
        return ResponseEntity
            .status(status)
            .body(new ErrorResponse(status.value(), messages));
    }

    private static boolean isDuplicateKey(Throwable ex) {
        Throwable cause = ex;

        while (cause != null) {
            String message = String.valueOf(cause.getMessage()).toLowerCase(Locale.ROOT);

            if (message.contains("duplicate key") || message.contains("already exists") || message.contains("unique")) {
                return true;
            }

            cause = cause.getCause();
        }

        return false;
    }

}
