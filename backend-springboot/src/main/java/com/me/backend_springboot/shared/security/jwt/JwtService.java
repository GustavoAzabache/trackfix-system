package com.me.backend_springboot.shared.security.jwt;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Service;

import com.me.backend_springboot.shared.security.exception.InvalidJwtException;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;

/**
 * Emite y valida tokens JWT firmados con HMAC-SHA256 (HS256).
 *
 * Claims de ambos tokens:
 *  - sub : id de la cuenta de autenticación (AuthEntity)
 *  - jti : UUID de la sesión (AuthSessionEntity) → identifica el dispositivo
 *  - type: "access" | "refresh" → se verifica en cada uso
 *  - iat / exp / iss
 */
@RequiredArgsConstructor
@Service
public class JwtService {

    public static final String TYPE_ACCESS = "access";
    public static final String TYPE_REFRESH = "refresh";

    private static final String CLAIM_TYPE = "type";

    private final JwtProperties jwtProperties;

    public String generateAccessToken(Long authId, UUID sessionId) {
        return buildToken(authId, sessionId, TYPE_ACCESS, jwtProperties.expirationAccess());
    }

    public String generateRefreshToken(Long authId, UUID sessionId) {
        return buildToken(authId, sessionId, TYPE_REFRESH, jwtProperties.expirationRefresh());
    }

    /** Valida un token de uso exclusivamente "access" y devuelve su contenido. */
    public JwtPayload parseAccessToken(String token) {
        return parse(token, TYPE_ACCESS);
    }

    /** Valida un token de uso exclusivamente "refresh" y devuelve su contenido. */
    public JwtPayload parseRefreshToken(String token) {
        return parse(token, TYPE_REFRESH);
    }

    private String buildToken(Long authId, UUID sessionId, String type, long expirationMillis) {
        Date now = new Date();

        return Jwts.builder()
                .subject(String.valueOf(authId))
                .id(sessionId.toString())
                .claim(CLAIM_TYPE, type)
                .issuer(jwtProperties.issuer())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationMillis))
                .signWith(signingKey(), Jwts.SIG.HS256)
                .compact();
    }

    private JwtPayload parse(String token, String expectedType) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String type = claims.get(CLAIM_TYPE, String.class);
            if (!expectedType.equals(type)) {
                throw new InvalidJwtException(
                        "Token de tipo '" + type + "' usado como '" + expectedType + "'.");
            }

            if (!jwtProperties.issuer().equals(claims.getIssuer())) {
                throw new InvalidJwtException("Emisor del token no reconocido.");
            }

            return new JwtPayload(
                    Long.valueOf(claims.getSubject()),
                    UUID.fromString(claims.getId()),
                    claims.getExpiration().toInstant());
        } catch (InvalidJwtException ex) {
            throw ex;
        } catch (JwtException | IllegalArgumentException ex) {
            // firma inválida, expirado, malformado, subject/jti ilegibles, etc.
            throw new InvalidJwtException("Token inválido o expirado.", ex);
        }
    }

    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8));
    }

}
