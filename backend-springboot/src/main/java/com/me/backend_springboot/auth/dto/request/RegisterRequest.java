package com.me.backend_springboot.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @NotBlank(message = "Email is required.")
    @Email(message = "Email must be a valid email address.")
    String email,

    @NotBlank(message = "Password is required.")
    @Size(min = 8, message = "Password must be at least 8 characters long.")
    String password,

    @NotBlank(message = "Tenant name is required.")
    String tenantName,

    // Opcional: el dominio lo valida solo si viene informado
    String tenantPhone,

    @NotBlank(message = "Tenant email is required.")
    @Email(message = "Tenant email must be a valid email address.")
    String tenantEmail,

    // Opcional: el dominio valida su formato solo si viene informado
    String tenantRuc,

    @NotBlank(message = "Tenant address is required.")
    String tenantAddress,

    @NotBlank(message = "Employee name is required.")
    String employeeName
) {}
