package com.me.backend_springboot.customer.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Datos del cliente embebidos en la orden. El cliente NO tiene cuenta ni
 * endpoints: esto es solo la entrada de datos.
 *
 * La regla "al menos un canal de contacto (phone o email)" NO se puede expresar
 * con Bean Validation estándar → se valida en el servicio/dominio.
 */
public record CreateCustomerRequest(
    @NotBlank(message = "Name is required.")
    String name,

    @NotBlank(message = "Last name is required.")
    String lastName,

    // Identidad: 8 dígitos como String (puede empezar con 0); unique en BD
    @NotBlank(message = "DNI is required.")
    String dni,

    // Solo contacto: opcional en DTO, el servicio exige al menos uno de phone/email
    String phone,

    @Email(message = "Email must be a valid email address.")
    String email
) {}
