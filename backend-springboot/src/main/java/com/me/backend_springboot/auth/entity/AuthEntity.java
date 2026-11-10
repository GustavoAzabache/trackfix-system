package com.me.backend_springboot.auth.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.me.backend_springboot.auth.exception.InvalidAuthDataException;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "auth")
public class AuthEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;
    
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static AuthEntity create(String email, String password) {
        validateData(email, password);

        LocalDateTime now = LocalDateTime.now();
        
        return new AuthEntity(
            null,
            email,
            password,
            now,
            now
        );
    }

    private static void validateData(String email, String password) {
        if (email == null || email.isBlank()) {
            throw new InvalidAuthDataException("El correo electrónico es obligatorio.");
        }

        if (!email.trim().matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")) {
            throw new InvalidAuthDataException("El formato del correo electrónico es incorrecto.");
        }

        if (password == null || password.isBlank()) {
            throw new InvalidAuthDataException("La contraseña es obligatoria.");
        }
    }

    @PreUpdate
    private void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
