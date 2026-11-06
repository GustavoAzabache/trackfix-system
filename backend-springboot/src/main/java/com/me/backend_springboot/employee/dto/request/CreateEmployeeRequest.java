package com.me.backend_springboot.employee.dto.request;

import com.me.backend_springboot.employee.entity.enums.HierarchyLevel;

public record CreateEmployeeRequest(
    String name,
    HierarchyLevel hierarchyLevel
) {}
