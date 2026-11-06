package com.me.backend_springboot.tenant.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.me.backend_springboot.tenant.entity.TenantEntity;

public interface TenantRepository extends JpaRepository<TenantEntity, Long> {

    Optional<TenantEntity> findByTenantCode(String tenantCode);
}
