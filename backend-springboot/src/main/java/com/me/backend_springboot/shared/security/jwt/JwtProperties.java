package com.me.backend_springboot.shared.security.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Propiedades JWT (prefijo "jwt" en application.yaml).
 *
 * @param secret            clave HMAC-SHA256 (mínimo 32 bytes)
 * @param expirationAccess  TTL del access token en milisegundos
 * @param expirationRefresh TTL del refresh token en milisegundos
 */
@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
    String secret,
    long expirationAccess,
    long expirationRefresh,
    String issuer
) {}
