package com.me.backend_springboot.shared.security.jwt;

import java.time.Instant;
import java.util.UUID;

/**
 * Contenido verificado de un token: siempre es del "type" esperado
 * (la firma y el tipo se validaron antes de construir este registro).
 *
 * @param authId    claim "sub" → cuenta de autenticación
 * @param sessionId claim "jti" → sesión (dispositivo) a la que pertenece el token
 * @param expiresAt claim "exp" → instante de expiración (base del TTL restante de la blacklist)
 */
public record JwtPayload(
    Long authId,
    UUID sessionId,
    Instant expiresAt
) {}
