package com.me.backend_springboot.customer.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.me.backend_springboot.customer.entity.CustomerEntity;

public interface CustomerRepository extends JpaRepository<CustomerEntity, Long> {

    // DNI = identidad del cliente: única clave del upsert
    Optional<CustomerEntity> findByDni(String dni);

}
