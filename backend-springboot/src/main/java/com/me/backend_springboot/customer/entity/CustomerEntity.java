package com.me.backend_springboot.customer.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "customers")
public class CustomerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private String name;

    private String lastName;

    @Column(nullable = true, unique = true)
    private String email;
    // Al menos uno de los campos deber ser ingresado, si ambos son null se invalida
    @Column(nullable = true)
    private String phone;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static CustomerEntity create(String name, String lastname, String email, String phone) {
        LocalDateTime now = LocalDateTime.now();

        return new CustomerEntity(
            null,
            name,
            lastname,
            email,
            phone,
            now,
            now
        );
    }

}
