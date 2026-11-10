package com.me.backend_springboot.employee.entity;

import java.time.LocalDateTime;

import com.me.backend_springboot.employee.entity.enums.HierarchyLevel;
import com.me.backend_springboot.employee.exception.InvalidEmployeeDataException;
import com.me.backend_springboot.tenant.entity.TenantEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "employees")
public class EmployeeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "hierarchy_level", nullable = false)
    @Enumerated(EnumType.STRING)
    private HierarchyLevel hierarchyLevel;

    @ManyToOne(fetch = FetchType.LAZY)
    // Si un taller es eliminado, se eliminan también sus empleados (cascade desde TenantEntity)
    @JoinColumn(name = "tenant_id", nullable = false)
    private TenantEntity tenant;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static EmployeeEntity create (String name, HierarchyLevel hierarchyLevel, TenantEntity tenant) {
        validateData(name, hierarchyLevel);

        LocalDateTime now = LocalDateTime.now();
        
        return new EmployeeEntity(
            null,
            name,
            hierarchyLevel,
            tenant,
            now,
            now
        );
    }

    private static void validateData(String name, HierarchyLevel hierarchyLevel) {
        if (name == null || name.isBlank()) {
            throw new InvalidEmployeeDataException("El nombre del empleado es obligatorio.");
        }

        if (hierarchyLevel == null) {
            throw new InvalidEmployeeDataException("El nivel jerárquico del empleado es obligatorio.");
        }
    }

    @PreUpdate
    private void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

}
