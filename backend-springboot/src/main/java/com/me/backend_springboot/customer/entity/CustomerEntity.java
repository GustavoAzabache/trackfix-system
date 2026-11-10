package com.me.backend_springboot.customer.entity;

import java.time.LocalDateTime;
import java.util.Locale;

import com.me.backend_springboot.customer.exception.CustomerDataException;
import com.me.backend_springboot.tenant.entity.TenantEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Cliente del negocio: NO tiene cuenta ni endpoints — entra como dato embebido
 * en la orden y solo consulta su estado con el código de rastreo.
 *
 * - tenant = el taller al que pertenece: el mismo DNI puede ser cliente de dos
 *   talleres distintos; los datos NO se comparten entre talleres (RF-03) y la
 *   identidad (DNI) es única POR TALLER.
 * - DNI = identidad del cliente dentro del taller: obligatorio, único e
 *   inmutable. Es la fuente de verdad porque el número de teléfono se recicla
 *   entre personas distintas.
 * - phone/email = solo canal de contacto para mandarle el código de rastreo
 *   (WhatsApp es el canal estrella); al menos uno es obligatorio.
 * - name/lastName = dato de presentación: se guardan tal cual los escribe el
 *   empleado (solo trim, sin tocar mayúsculas/minúsculas).
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "customers", uniqueConstraints = @UniqueConstraint(columnNames = { "tenant_id", "dni" }))
public class CustomerEntity {

    private static final String DNI_REGEX = "^\\d{8}$";
    private static final String EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$";
    private static final String PHONE_REGEX = "^9\\d{8}$";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String lastName;

    // Identidad del cliente DENTRO del taller: 8 dígitos, única por tenant y jamás se actualiza
    @Column(name = "dni", nullable = false, updatable = false)
    private String dni;

    // Solo contacto: sin unique (dos personas pueden compartir correo familiar)
    @Column(nullable = true)
    private String email;

    // Solo contacto (WhatsApp): nunca identidad — los números se reciclan
    @Column(nullable = true)
    private String phone;

    // El taller al que pertenece: sin él no hay aislamiento entre talleres (RF-03)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private TenantEntity tenant;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static CustomerEntity create(
            String name, String lastName, String dni, String email, String phone, TenantEntity tenant) {
        // Normaliza ANTES de validar: lo que se guarda ya viene limpio
        String normalizedName = trimToNull(name);
        String normalizedLastName = trimToNull(lastName);
        String normalizedDni = trimToNull(dni);
        String normalizedEmail = normalizeEmail(email);
        String normalizedPhone = normalizePhone(phone);

        validate(normalizedName, normalizedLastName, normalizedDni, normalizedEmail, normalizedPhone, tenant);

        LocalDateTime now = LocalDateTime.now();

        return new CustomerEntity(
            null,
            normalizedName,
            normalizedLastName,
            normalizedDni,
            normalizedEmail,
            normalizedPhone,
            tenant,
            now,
            now
        );
    }

    /**
     * Update parcial: solo se aplican los campos que vengan con dato (vacío tras
     * normalizar = se conserva el actual). Así nunca se pierde un canal de
     * contacto que ya se tenía. El DNI no se toca (es la identidad), el taller
     * tampoco (el cliente no cambia de taller) y updatedAt lo refresca
     * @PreUpdate cuando Hibernate emite el UPDATE.
     */
    public void update(String name, String lastName, String email, String phone) {
        String nextName = trimToNull(name);
        String nextLastName = trimToNull(lastName);
        String nextEmail = normalizeEmail(email);
        String nextPhone = normalizePhone(phone);

        if (nextName == null) {
            nextName = this.name;
        }
        if (nextLastName == null) {
            nextLastName = this.lastName;
        }
        if (nextEmail == null) {
            nextEmail = this.email;
        }
        if (nextPhone == null) {
            nextPhone = this.phone;
        }

        // Valida el resultado mergeado: el update no puede romper las invariantes
        validate(nextName, nextLastName, this.dni, nextEmail, nextPhone, this.tenant);

        this.name = nextName;
        this.lastName = nextLastName;
        this.email = nextEmail;
        this.phone = nextPhone;
    }

    /** JPA: se ejecuta solo cuando Hibernate emite el UPDATE — refresca updatedAt sin exponerlo en update(). */
    @PreUpdate
    private void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    private static void validate(
            String name, String lastName, String dni, String email, String phone, TenantEntity tenant) {
        // Aislamiento entre talleres (RF-03): sin taller no hay dónde ubicar al cliente
        if (tenant == null) {
            throw new CustomerDataException("El taller es obligatorio.");
        }

        if (!hasText(name)) {
            throw new CustomerDataException("El nombre es obligatorio.");
        }

        if (!hasText(lastName)) {
            throw new CustomerDataException("El apellido es obligatorio.");
        }

        if (!hasText(dni)) {
            throw new CustomerDataException("El DNI es obligatorio.");
        }

        if (!dni.matches(DNI_REGEX)) {
            throw new CustomerDataException("El DNI debe tener exactamente 8 dígitos.");
        }

        // Diseño: siempre hace falta una vía para mandarle el código de rastreo
        if (!hasText(email) && !hasText(phone)) {
            throw new CustomerDataException("Se requiere al menos un canal de contacto (teléfono o correo electrónico).");
        }

        if (hasText(email) && !email.matches(EMAIL_REGEX)) {
            throw new CustomerDataException("El formato del correo electrónico es incorrecto.");
        }

        if (hasText(phone) && !phone.matches(PHONE_REGEX)) {
            throw new CustomerDataException("El celular debe tener exactamente 9 dígitos y comenzar con 9, sin código de país.");
        }
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    // name/lastName/dni: SOLO trim — name es dato de presentación (jamás se toca
    // mayúsculas/minúsculas: "brandom" se guarda tal cual); en blanco → null
    // para que validate() reporte el campo como obligatorio.
    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    // Contacto: trim + minúsculas — el correo se guarda siempre en minúsculas
    private static String normalizeEmail(String value) {
        String trimmed = trimToNull(value);
        return trimmed == null ? null : trimmed.toLowerCase(Locale.ROOT);
    }

    // Contacto: sin espacios — el celular peruano son 9 dígitos puros (WhatsApp)
    private static String normalizePhone(String value) {
        String trimmed = trimToNull(value);
        if (trimmed == null) {
            return null;
        }

        String digits = trimmed.replaceAll("\\s+", "");
        return digits.isEmpty() ? null : digits;
    }

}
