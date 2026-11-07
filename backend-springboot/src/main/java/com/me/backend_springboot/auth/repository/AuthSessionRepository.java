package com.me.backend_springboot.auth.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.me.backend_springboot.auth.entity.AuthSessionEntity;

public interface AuthSessionRepository extends JpaRepository<AuthSessionEntity, UUID> {

    List<AuthSessionEntity> findByAuthId(Long authId);

}
