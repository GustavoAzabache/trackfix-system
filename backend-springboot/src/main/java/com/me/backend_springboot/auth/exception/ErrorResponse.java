package com.me.backend_springboot.auth.exception;

import java.time.LocalDateTime;
import java.util.List;

public record ErrorResponse(
    int status,
    LocalDateTime timestamp,
    List<String> messages
) {}
