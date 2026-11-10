package com.me.backend_springboot.shared.exception;

import java.time.LocalDateTime;
import java.util.List;

public record ErrorResponse(
    int status,
    LocalDateTime timestamp,
    List<String> messages
) {

    // Un solo mensaje (la mayoría de los handlers)
    public ErrorResponse(int status, String message) {
        this(status, LocalDateTime.now(), List.of(message));
    }

    // Varios mensajes (p. ej. MethodArgumentNotValidException: uno por campo inválido)
    public ErrorResponse(int status, List<String> messages) {
        this(status, LocalDateTime.now(), messages);
    }

}
