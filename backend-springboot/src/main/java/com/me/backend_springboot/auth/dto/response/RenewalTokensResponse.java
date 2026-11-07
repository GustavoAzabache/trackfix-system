package com.me.backend_springboot.auth.dto.response;

public record RenewalTokensResponse(
    String accessToken,
    String refreshToken
) {}
