package com.me.backend_springboot.auth.service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.me.backend_springboot.auth.dto.request.LoginRequest;
import com.me.backend_springboot.auth.dto.request.RefreshRequest;
import com.me.backend_springboot.auth.dto.request.RegisterRequest;
import com.me.backend_springboot.auth.dto.response.AuthenticationResponse;
import com.me.backend_springboot.auth.dto.response.RenewalTokensResponse;
import com.me.backend_springboot.auth.entity.AuthEntity;
import com.me.backend_springboot.auth.entity.AuthSessionEntity;
import com.me.backend_springboot.auth.exception.EmailAlreadyRegisteredException;
import com.me.backend_springboot.auth.exception.InvalidAuthDataException;
import com.me.backend_springboot.auth.exception.InvalidCredentialsException;
import com.me.backend_springboot.auth.exception.SessionNotActiveException;
import com.me.backend_springboot.auth.repository.AuthRepository;
import com.me.backend_springboot.auth.repository.AuthSessionRepository;
import com.me.backend_springboot.employee.dto.request.CreateEmployeeRequest;
import com.me.backend_springboot.employee.entity.enums.HierarchyLevel;
import com.me.backend_springboot.employee.service.EmployeeService;
import com.me.backend_springboot.shared.security.jwt.JwtPayload;
import com.me.backend_springboot.shared.security.jwt.JwtProperties;
import com.me.backend_springboot.shared.security.jwt.JwtService;
import com.me.backend_springboot.shared.security.jwt.TokenBlacklistService;
import com.me.backend_springboot.tenant.dto.request.CreateTenantRequest;
import com.me.backend_springboot.tenant.entity.TenantEntity;
import com.me.backend_springboot.tenant.service.TentantService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class AuthService {

    private static final String BEARER_PREFIX = "Bearer ";

    private final AuthRepository authRepository;
    private final AuthSessionRepository authSessionRepository;
    private final TentantService tentantService;
    private final EmployeeService employeeService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final TokenBlacklistService tokenBlacklistService;

    @Transactional
    public AuthenticationResponse register(RegisterRequest request, String device) {
        String email = normalizeEmail(request.email());

        // Blindaje previo: encode(null) revienta y generate("") pasaría la validación del dominio
        if (request.password() == null || request.password().isBlank()) {
            throw new InvalidAuthDataException("La contraseña es obligatoria.");
        }

        // Valida formato de correo y contraseña; se persiste solo el hash BCrypt
        AuthEntity auth = AuthEntity.create(email, passwordEncoder.encode(request.password()));

        if (authRepository.existsByEmail(email)) {
            throw new EmailAlreadyRegisteredException();
        }

        auth = authRepository.save(auth);

        TenantEntity tenant = tentantService.create(
            new CreateTenantRequest(
                request.tenantName(),
                request.tenantPhone(),
                request.tenantEmail(),
                request.tenantRuc(),
                request.tenantAddress(),
                auth
            )
        );

        employeeService.create(
            tenant,
            new CreateEmployeeRequest(
                request.employeeName(),
                HierarchyLevel.PRINCIPAL
            )
        );

        // El registro también deja la sesión iniciada: se devuelven los tokens
        AuthSessionEntity session = createSession(auth, device);

        return new AuthenticationResponse(
            jwtService.generateAccessToken(auth.getId(), session.getId()),
            jwtService.generateRefreshToken(auth.getId(), session.getId())
        );
    }

    @Transactional
    public AuthenticationResponse login(LoginRequest request, String device) {
        String email = normalizeEmail(request.email());

        AuthEntity auth = authRepository.findByEmail(email)
                .filter(a -> passwordEncoder.matches(request.password(), a.getPasswordHash()))
                .orElseThrow(InvalidCredentialsException::new);

        AuthSessionEntity session = createSession(auth, device);

        return new AuthenticationResponse(
            jwtService.generateAccessToken(auth.getId(), session.getId()),
            jwtService.generateRefreshToken(auth.getId(), session.getId())
        );
    }

    /**
     * Rotación: el refresh recibido se revoca (por su jti/sesión) y se emite
     * una pareja totalmente nueva atada a una sesión nueva (jti nuevo).
     */
    @Transactional
    public RenewalTokensResponse refresh(RefreshRequest request, String device) {
        JwtPayload payload = jwtService.parseRefreshToken(request.refreshToken());

        AuthSessionEntity currentSession = authSessionRepository.findById(payload.sessionId())
                .orElseThrow(SessionNotActiveException::new);

        if (!currentSession.isActive()) {
            throw new SessionNotActiveException();
        }

        currentSession.revoke();
        // El jti viejo (access de la pareja anterior) entra a la blacklist con el TTL del
        // access: el token anterior vive a lo sumo 15 min y la key no ensucia Redis.
        tokenBlacklistService.blacklist(
                payload.sessionId(), Duration.ofMillis(jwtProperties.expirationAccess()));

        AuthSessionEntity newSession = createSession(currentSession.getAuth(), device);

        Long authId = newSession.getAuth().getId();
        return new RenewalTokensResponse(
            jwtService.generateAccessToken(authId, newSession.getId()),
            jwtService.generateRefreshToken(authId, newSession.getId())
        );
    }

    /** Logout local: revoca solo la sesión del token presentado (ese dispositivo). */
    @Transactional
    public void logout(String authorizationHeader) {
        JwtPayload payload = jwtService.parseAccessToken(extractBearerToken(authorizationHeader));

        authSessionRepository.findById(payload.sessionId())
                .ifPresent(AuthSessionEntity::revoke);

        // TTL restante exacto del access (exp − ahora): la key muere sola cuando el token vence
        tokenBlacklistService.blacklist(
            payload.sessionId(), Duration.between(Instant.now(), payload.expiresAt())
        );
    }

    private AuthSessionEntity createSession(AuthEntity auth, String device) {
        LocalDateTime expiresAt = LocalDateTime.now()
                .plus(jwtProperties.expirationRefresh(), ChronoUnit.MILLIS);

        return authSessionRepository.save(AuthSessionEntity.create(auth, device, expiresAt));
    }

    private static String extractBearerToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            return null;
        }

        String token = authorizationHeader.substring(BEARER_PREFIX.length()).trim();
        return token.isEmpty() ? null : token;
    }

    private static String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }

        return email.trim().toLowerCase(Locale.ROOT);
    }

}
