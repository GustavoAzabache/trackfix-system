package com.me.backend_springboot.tenant.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.me.backend_springboot.tenant.dto.request.CreateTenantRequest;
import com.me.backend_springboot.tenant.dto.response.CreateTenantResponse;
import com.me.backend_springboot.tenant.entity.TenantEntity;
import com.me.backend_springboot.tenant.service.TentantService;

import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/tenants")
public class TenantController {

    private final TentantService tentantService;

    // Endpoint de prueba. Se deberá eliminar después
    // Un tenant se crea en el mismo flujo de registro de una cuenta (en auth)
    @PostMapping
    public ResponseEntity<CreateTenantResponse> create(@RequestBody CreateTenantRequest request) {
        TenantEntity tenant = tentantService.create(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(CreateTenantResponse.from(tenant));
    }
    
}
