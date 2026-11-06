package com.me.backend_springboot.tenant.dto.request;

import com.me.backend_springboot.auth.entity.AuthEntity;

public record CreateTenantRequest(
    String name, String phone, String email, String ruc, String address, AuthEntity auth
) {}
