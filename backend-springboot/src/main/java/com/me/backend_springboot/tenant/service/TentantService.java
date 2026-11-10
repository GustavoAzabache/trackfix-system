package com.me.backend_springboot.tenant.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.me.backend_springboot.tenant.dto.request.CreateTenantRequest;
import com.me.backend_springboot.tenant.entity.TenantEntity;
import com.me.backend_springboot.tenant.repository.TenantRepository;
import com.me.backend_springboot.tenant.utils.Base36Generator;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class TentantService {

    private final TenantRepository tenantRepository;

    @Transactional
    public TenantEntity create(CreateTenantRequest request) {
        TenantEntity tenant = TenantEntity.create(
            Base36Generator.generate(),
            request.name(),
            request.phone(),
            request.email(),
            request.ruc(),
            request.address(),
            request.auth()
        );

        return tenantRepository.save(tenant);
    }

}
