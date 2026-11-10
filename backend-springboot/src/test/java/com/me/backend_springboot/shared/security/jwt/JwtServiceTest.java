package com.me.backend_springboot.shared.security.jwt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.me.backend_springboot.shared.security.exception.InvalidJwtException;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

public class JwtServiceTest {

    private static final String SECRET = "XGGOIW0xbvsp6kMR6tjbrnq4nIU1vPA6N/bcqu78U4o="; // 44 bytes ≥ 32 (HS256)
    private static final long ACCESS_TTL = 900_000L;        // 15 min
    private static final long REFRESH_TTL = 2_592_000_000L; // 30 días
    private static final String ISSUER = "trackfix_system";

    private final JwtService service = new JwtService(new JwtProperties(SECRET, ACCESS_TTL, REFRESH_TTL, ISSUER));

    private final Long authId = 7L;
    private final UUID sessionId = UUID.randomUUID();

    @Test
    void shouldGenerateAccessTokenWithSessionPayload() {
        String token = service.generateAccessToken(authId, sessionId);

        JwtPayload payload = service.parseAccessToken(token);

        assertEquals(authId, payload.authId());
        assertEquals(sessionId, payload.sessionId());
    }

    @Test
    void shouldGenerateRefreshTokenWithSessionPayload() {
        String token = service.generateRefreshToken(authId, sessionId);

        JwtPayload payload = service.parseRefreshToken(token);

        assertEquals(authId, payload.authId());
        assertEquals(sessionId, payload.sessionId());
    }

    @Test
    void shouldRejectAccessTokenUsedAsRefresh() {
        String accessToken = service.generateAccessToken(authId, sessionId);

        InvalidJwtException ex = assertThrows(InvalidJwtException.class,
                () -> service.parseRefreshToken(accessToken));

        assertEquals("Token de tipo 'access' usado como 'refresh'.", ex.getMessage());
    }

    @Test
    void shouldRejectRefreshTokenUsedAsAccess() {
        String refreshToken = service.generateRefreshToken(authId, sessionId);

        InvalidJwtException ex = assertThrows(InvalidJwtException.class,
                () -> service.parseAccessToken(refreshToken));

        assertEquals("Token de tipo 'refresh' usado como 'access'.", ex.getMessage());
    }

    @Test
    void shouldRejectExpiredToken() {
        JwtService expiredService = new JwtService(new JwtProperties(SECRET, -5_000L, REFRESH_TTL, ISSUER));

        String expiredToken = expiredService.generateAccessToken(authId, sessionId);

        assertThrows(InvalidJwtException.class, () -> service.parseAccessToken(expiredToken));
    }

    @Test
    void shouldRejectTokenSignedWithAnotherSecret() {
        String anotherSecret = "otro-secreto-completamente-distinto-1234";
        JwtService otherService = new JwtService(new JwtProperties(anotherSecret, ACCESS_TTL, REFRESH_TTL, ISSUER));

        String foreignToken = otherService.generateAccessToken(authId, sessionId);

        assertThrows(InvalidJwtException.class, () -> service.parseAccessToken(foreignToken));
    }

    @Test
    void shouldRejectTokenWithAnotherIssuer() {
        String forged = Jwts.builder()
                .subject(String.valueOf(authId))
                .id(sessionId.toString())
                .claim("type", "access")
                .issuer("evil-issuer")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60_000L))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)), Jwts.SIG.HS256)
                .compact();

        InvalidJwtException ex = assertThrows(InvalidJwtException.class,
                () -> service.parseAccessToken(forged));

        assertEquals("Emisor del token no reconocido.", ex.getMessage());
    }

}
