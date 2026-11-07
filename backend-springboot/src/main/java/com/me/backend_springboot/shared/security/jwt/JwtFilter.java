package com.me.backend_springboot.shared.security.jwt;

import java.io.IOException;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.me.backend_springboot.shared.security.exception.InvalidJwtException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * Autentica cada request que traiga un access token en el header "Authorization: Bearer …".
 *
 * Comportamiento:
 *  - Sin header o sin prefijo Bearer  → no autentica; la cadena sigue.
 *  - Token válido                     → setea el SecurityContext con el authId (sub) como principal,
 *                                       sin authorities (sin roles en esta fase).
 *  - Token inválido/expirado (InvalidJwtException) → no autentica y sigue la cadena: si el
 *    endpoint es protegido, el AuthenticationEntryPoint responde 401; si es público, responde normal.
 *
 * El chequeo de la blacklist en Redis se agrega en la fase 6 (fail-closed).
 */
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            authenticateIfPresent(request);
            filterChain.doFilter(request, response);
        } finally {
            // Higiene: el contexto no debe sobrevivir al request (evita fugas entre requests)
            SecurityContextHolder.clearContext();
        }
    }

    private void authenticateIfPresent(HttpServletRequest request) {
        String token = extractBearerToken(request);
        if (token == null) {
            return;
        }

        JwtPayload payload;
        try {
            payload = jwtService.parseAccessToken(token);
        } catch (InvalidJwtException ex) {
            // Opción A: no se autentica y se continúa la cadena; el 401 (si aplica) lo resuelve el entry point.
            return;
        }

        try {
            if (tokenBlacklistService.isBlacklisted(payload.sessionId())) {
                // Revocado por logout o rotación: no entra
                return;
            }
        } catch (DataAccessException ex) {
            // Fail-closed: Redis inaccesible → no se puede verificar la revocación → no se autentica
            return;
        }

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(payload.authId(), null, List.of()));
    }

    private String extractBearerToken(HttpServletRequest request) {
        String header = request.getHeader(AUTHORIZATION_HEADER);
        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            return null;
        }

        String token = header.substring(BEARER_PREFIX.length()).trim();
        return token.isEmpty() ? null : token;
    }

}
