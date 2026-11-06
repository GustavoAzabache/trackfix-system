package com.me.backend_springboot.employee.service;

import org.springframework.stereotype.Service;

import com.me.backend_springboot.employee.dto.request.CreateEmployeeRequest;
import com.me.backend_springboot.employee.dto.response.CreateEmployeeResponse;
import com.me.backend_springboot.employee.entity.EmployeeEntity;
import com.me.backend_springboot.employee.repository.EmployeeRepository;
import com.me.backend_springboot.tenant.entity.TenantEntity;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    public CreateEmployeeResponse create(TenantEntity tenant, CreateEmployeeRequest request) {
        EmployeeEntity employee = employeeRepository.save(
            EmployeeEntity.create(request.name(), request.hierarchyLevel(), tenant)
        );

        return new CreateEmployeeResponse(
            employee.getName(),
            employee.getHierarchyLevel().name(),
            employee.getCreatedAt()
        );
    }

}
