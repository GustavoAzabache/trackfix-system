package com.me.backend_springboot.auth.service;

import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.me.backend_springboot.auth.dto.request.RegisterRequest;
import com.me.backend_springboot.auth.dto.response.RegisterResponse;
import com.me.backend_springboot.auth.entity.AuthEntity;
import com.me.backend_springboot.auth.exception.EmailAlreadyRegisteredException;
import com.me.backend_springboot.auth.repository.AuthRepository;
import com.me.backend_springboot.employee.dto.request.CreateEmployeeRequest;
import com.me.backend_springboot.employee.dto.response.CreateEmployeeResponse;
import com.me.backend_springboot.employee.entity.enums.HierarchyLevel;
import com.me.backend_springboot.employee.service.EmployeeService;
import com.me.backend_springboot.tenant.dto.request.CreateTenantRequest;
import com.me.backend_springboot.tenant.dto.response.CreateTenantResponse;
import com.me.backend_springboot.tenant.entity.TenantEntity;
import com.me.backend_springboot.tenant.service.TentantService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class AuthService {

    private final AuthRepository authRepository;    
    private final TentantService tentantService;
    private final EmployeeService employeeService;

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());

        // Valida formato de correo y contraseña antes de tocar la base de datos
        AuthEntity auth = AuthEntity.create(email, request.password());

        if (authRepository.existsByEmail(email)) {
            throw new EmailAlreadyRegisteredException();
        }

        auth = authRepository.save(auth);

        TenantEntity tenant = tentantService.create(
            new CreateTenantRequest(
                request.tenantName(),
                request.tenantPhone(),
                request.tenantEmail(),
                request.tenantRuc(),
                request.tenantAddress(),
                auth
            )
        );

        CreateEmployeeResponse employeeResponse = employeeService.create(
            tenant,
            new CreateEmployeeRequest(
                request.employeeName(),
                HierarchyLevel.PRINCIPAL
            )
        );

        return new RegisterResponse(
            auth.getId(),
            auth.getEmail(),
            CreateTenantResponse.from(tenant),
            employeeResponse
        );
    }

    private static String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }

        return email.trim().toLowerCase(Locale.ROOT);
    }

}
