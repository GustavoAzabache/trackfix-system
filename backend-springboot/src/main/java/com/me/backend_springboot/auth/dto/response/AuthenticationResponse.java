package com.me.backend_springboot.auth.dto.response;

public record AuthenticationResponse(
    String accessToken,
    String refreshToken
) {}
