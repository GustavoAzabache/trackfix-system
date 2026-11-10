package com.me.backend_springboot.shared.security.jwt;

import java.time.Duration;
import java.util.UUID;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

/**
 * Blacklist de access tokens en Redis: guarda el "jti" (id de sesión) de los tokens
 * revocados (logout y rotación del refresh) con su TTL para que el JwtFilter los rechace.
 *
 * - Las claves expiran solas con su TTL: no hay limpieza programada.
 * - Fail-closed: si Redis no responde, isBlacklisted lanza y el filtro no autentica.
 */
@RequiredArgsConstructor
@Service
public class TokenBlacklistService {

    private static final String KEY_PREFIX = "jwt:blacklist:";

    private final StringRedisTemplate redis;

    /** Pone el jti en la blacklist. Si el token ya venció (TTL ≤ 0), no hay nada que bloquear. */
    public void blacklist(UUID sessionId, Duration ttl) {
        if (ttl == null || ttl.isNegative() || ttl.isZero()) {
            return;
        }

        redis.opsForValue().set(KEY_PREFIX + sessionId, "revoked", ttl);
    }

    /** ¿Está revocado este jti? Lanza DataAccessException si Redis está inaccesible. */
    public boolean isBlacklisted(UUID sessionId) {
        return Boolean.TRUE.equals(redis.hasKey(KEY_PREFIX + sessionId));
    }

}
