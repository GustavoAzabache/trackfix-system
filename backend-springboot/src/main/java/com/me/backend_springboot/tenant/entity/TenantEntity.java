package com.me.backend_springboot.tenant.entity;

import java.time.LocalDateTime;
import java.util.List;

import com.me.backend_springboot.auth.entity.AuthEntity;
import com.me.backend_springboot.employee.entity.EmployeeEntity;
import com.me.backend_springboot.tenant.exception.InvalidPhoneException;
import com.me.backend_springboot.tenant.exception.InvalidRucException;
import com.me.backend_springboot.tenant.exception.InvalidTenantCodeException;
import com.me.backend_springboot.tenant.exception.InvalidTenantDataException;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "tenants")
public class TenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(name = "tenant_code", nullable = false, unique = true)
    private String tenantCode;

    @Column(nullable = false, unique = true)
    private String name;

    // Opcional: si no viene, la columna admite NULL (los NULL no chocan con el unique)
    @Column(nullable = true, unique = true)
    private String phone;

    @Column(nullable = true, unique = true)
    private String email; // correo del negocio, con este no se autentica

    @Column(nullable = true, unique = true)
    private String ruc;

    @Column(nullable = false)
    private String address;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auth_id", nullable = false)
    private AuthEntity auth;

    // Si un taller es eliminado, se eliminan también sus empleados
    @OneToMany(mappedBy = "tenant", cascade = CascadeType.REMOVE)
    private List<EmployeeEntity> employees;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static TenantEntity create(String tenantCode, String name, String phone, String email, String ruc, String address, AuthEntity auth) {
        validateTenantCode(tenantCode);
        validateData(name, phone, email, ruc, address);

        LocalDateTime now = LocalDateTime.now();

        return new TenantEntity(
            null,
            tenantCode,
            name,
            normalizePhone(phone),
            email,
            ruc,
            address,
            auth,
            null,
            now,
            now
        );
    }

    @PreUpdate
    private void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    private static void validateTenantCode(String tenantCode) {
        if (tenantCode == null || tenantCode.trim().length() != 4) {
            throw new InvalidTenantCodeException("El código del taller debe tener exactamente 4 caracteres alfanuméricos.");
        }
    }

    // Celular sin espacios y en dígitos puros; vacío u omitido → null
    private static String normalizePhone(String phone) {
        if (phone == null || phone.isBlank()) {
            return null;
        }

        return phone.trim().replaceAll("\\s+", "");
    }

    private static void validateData(String name, String phone, String email, String ruc, String address) {
        if (name == null || name.isBlank() ||
            email == null || email.isBlank() ||
            address == null || address.isBlank()) {
            throw new InvalidTenantDataException("Todos los campos obligatorios (Nombre, Email, Dirección) deben ser completados.");
        }

        if (ruc != null && !ruc.isBlank()) {
            String cleanRuc = ruc.trim();
            if (!cleanRuc.matches("^(10|20)\\d{9}$")) {
                throw new InvalidRucException("El RUC ingresado no es válido. Debe tener 11 dígitos numéricos y comenzar con 10 o 20.");
            }
        }

        if (phone != null && !phone.isBlank()) {
            String cleanPhone = normalizePhone(phone);

            // Solo celular peruano: 9 dígitos que comienzan con 9, sin +51
            if (!cleanPhone.matches("^9\\d{8}$")) {
                throw new InvalidPhoneException("El celular debe tener exactamente 9 dígitos y comenzar con 9, sin código de país.");
            }
        }

        if (!email.trim().matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")) {
            throw new InvalidTenantDataException("El formato del correo electrónico es incorrecto.");
        }

        if (address.trim().length() < 10) {
            throw new InvalidTenantDataException("La dirección proporcionada es demasiado corta. Debe incluir calle, número, distrito y provincia.");
        }
    }

}
