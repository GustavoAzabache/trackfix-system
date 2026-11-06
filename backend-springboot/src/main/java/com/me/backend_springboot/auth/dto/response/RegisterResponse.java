package com.me.backend_springboot.auth.dto.response;

import com.me.backend_springboot.employee.dto.response.CreateEmployeeResponse;
import com.me.backend_springboot.tenant.dto.response.CreateTenantResponse;

public record RegisterResponse(
    Long id,
    String email,
    CreateTenantResponse tenantResponse,
    CreateEmployeeResponse employeeResponse
) {}
