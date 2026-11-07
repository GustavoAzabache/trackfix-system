package com.me.backend_springboot.auth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.me.backend_springboot.auth.entity.AuthEntity;

public interface AuthRepository extends JpaRepository<AuthEntity, Long> {
    boolean existsByEmail(String email);

    Optional<AuthEntity> findByEmail(String email);
}
