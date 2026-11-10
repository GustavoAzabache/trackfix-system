package com.me.backend_springboot.customer.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.me.backend_springboot.customer.entity.CustomerEntity;
import com.me.backend_springboot.tenant.entity.TenantEntity;

public interface CustomerRepository extends JpaRepository<CustomerEntity, Long> {

    // DNI = identidad del cliente DENTRO del taller: la clave del upsert es
    // (tenant, dni) — el mismo DNI puede existir en otro taller (RF-03)
    Optional<CustomerEntity> findByDniAndTenant(String dni, TenantEntity tenant);

}
