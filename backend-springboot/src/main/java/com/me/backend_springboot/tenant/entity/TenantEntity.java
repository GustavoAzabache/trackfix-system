package com.me.backend_springboot.tenant.entity;

import java.time.LocalDateTime;

import com.me.backend_springboot.tenant.exception.InvalidPhoneException;
import com.me.backend_springboot.tenant.exception.InvalidRucException;
import com.me.backend_springboot.tenant.exception.InvalidTenantCodeException;
import com.me.backend_springboot.tenant.exception.InvalidTenantDataException;

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

    @Column(nullable = false, unique = true)
    private String phone;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = true, unique = true)
    private String ruc;

    @Column(nullable = false)
    private String address;

    @Column(name = "created_at", nullable = false, updatable = true)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static TenantEntity create(String tenatCode, String name, String phone, String email, String ruc, String addres) {
        validateData(tenatCode, name, phone, email, ruc, addres);
        
        LocalDateTime now = LocalDateTime.now();

        return new TenantEntity(
            null,
            tenatCode,
            name,
            phone,
            email,
            ruc,
            addres,
            now,
            now
        );
    }

    @PreUpdate
    private void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    private static void validateData(String tenantCode, String name, String phone, String email, String ruc, String address) {
        if (tenantCode == null || tenantCode.isBlank() ||
            name == null || name.isBlank() ||
            email == null || email.isBlank() ||
            address == null || address.isBlank()) {
            throw new InvalidTenantDataException("Todos los campos obligatorios (Código, Nombre, RUC, Email, Dirección) deben ser completados.");
        }
        
        if (tenantCode.trim().length() != 4) {
            throw new InvalidTenantCodeException("El código del taller debe tener exactamente 4 caracteres alfanuméricos.");
        }

        if (ruc != null && !ruc.isBlank()) {
            String cleanRuc = ruc.trim();
            if (!cleanRuc.matches("^(10|20)\\d{9}$")) {
                throw new InvalidRucException("El RUC ingresado no es válido. Debe tener 11 dígitos numéricos y comenzar con 10 o 20.");
            }
        }

        if (phone != null && !phone.isBlank()) {
            String cleanPhone = phone.trim().replaceAll("\\s+", "");
            
            // Celulares en Perú empiezan con 9 y tienen 9 dígitos. 
            // Teléfonos fijos tienen 7 u 8 dígitos (dependiendo de la provincia, ej: Lima es 01 + 7 dígitos).
            boolean isMobile = cleanPhone.matches("^9\\d{8}$");
            boolean isLandline = cleanPhone.matches("^\\d{7,9}$");
            
            if (!isMobile && !isLandline) {
                throw new InvalidPhoneException("El número de teléfono no es válido para el territorio peruano.");
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
