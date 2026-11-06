package com.me.backend_springboot.auth.dto.request;

public record RegisterRequest(
    String email,
    String password,
    String tenantName,
    String tenantPhone,
    String tenantEmail,
    String tenantRuc,
    String tenantAddress,
    String employeeName
) {}
