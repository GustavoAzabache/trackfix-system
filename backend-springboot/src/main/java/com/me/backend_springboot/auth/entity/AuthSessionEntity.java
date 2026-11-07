package com.me.backend_springboot.auth.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Sesión de autenticación: una fila por dispositivo/dispositivo logueado.
 * Su id (UUID) viaja como claim "jti" de los access y refresh tokens.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "auth_sessions")
public class AuthSessionEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auth_id", nullable = false)
    private AuthEntity auth;

    @Column(nullable = true)
    private String device; // User-Agent informativo: identifica el dispositivo de la sesión

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at", nullable = false, updatable = false)
    private LocalDateTime expiresAt; // vence junto con el refresh token (30 días)

    @Column(nullable = false)
    private boolean revoked;

    @Column(name = "revoked_at", nullable = true)
    private LocalDateTime revokedAt;

    public static AuthSessionEntity create(AuthEntity auth, String device, LocalDateTime expiresAt) {
        return new AuthSessionEntity(
            UUID.randomUUID(),
            auth,
            device,
            LocalDateTime.now(),
            expiresAt,
            false,
            null
        );
    }

    /** Revoca la sesión: invalida su refresh token de inmediato. */
    public void revoke() {
        if (!revoked) {
            this.revoked = true;
            this.revokedAt = LocalDateTime.now();
        }
    }

    public boolean isActive() {
        return !revoked && expiresAt.isAfter(LocalDateTime.now());
    }

}
