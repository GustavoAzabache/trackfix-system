package com.me.backend_springboot.tenant.dto.response;

import java.time.LocalDateTime;

import com.me.backend_springboot.tenant.entity.TenantEntity;

public record CreateTenantResponse(
    String tenantCode,
    String name,
    String phone,
    String email,
    String ruc,
    String address,
    LocalDateTime createdAt
) {

    public static CreateTenantResponse from(TenantEntity tenant) {
        return new CreateTenantResponse(
            tenant.getTenantCode(),
            tenant.getName(),
            tenant.getPhone(),
            tenant.getEmail(),
            tenant.getRuc(),
            tenant.getAddress(),
            tenant.getCreatedAt()
        );
    }

}
