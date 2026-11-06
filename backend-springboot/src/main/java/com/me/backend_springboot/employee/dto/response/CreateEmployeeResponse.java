package com.me.backend_springboot.employee.dto.response;

import java.time.LocalDateTime;

public record CreateEmployeeResponse(
    String name,
    String hierarchyLevel,
    LocalDateTime createdAt
) {}
