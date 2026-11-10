package com.me.backend_springboot.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record RefreshRequest(
    @NotBlank(message = "Refresh token is required.")
    String refreshToken
) {}
