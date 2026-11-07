package com.me.backend_springboot.auth.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.me.backend_springboot.auth.dto.request.LoginRequest;
import com.me.backend_springboot.auth.dto.request.RefreshRequest;
import com.me.backend_springboot.auth.dto.request.RegisterRequest;
import com.me.backend_springboot.auth.dto.response.AuthenticationResponse;
import com.me.backend_springboot.auth.dto.response.RenewalTokensResponse;
import com.me.backend_springboot.auth.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(
            @Valid @RequestBody RegisterRequest request,
            @RequestHeader(value = "User-Agent", required = false) String device) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request, device));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(
            @Valid @RequestBody LoginRequest request,
            @RequestHeader(value = "User-Agent", required = false) String device) {
        return ResponseEntity.ok(authService.login(request, device));
    }

    @PostMapping("/refresh")
    public ResponseEntity<RenewalTokensResponse> refresh(
            @Valid @RequestBody RefreshRequest request,
            @RequestHeader(value = "User-Agent", required = false) String device) {
        return ResponseEntity.ok(authService.refresh(request, device));
    }

    // Requiere access token válido (no está en permitAll): el JwtFilter ya autenticó la petición
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String authorization) {
        authService.logout(authorization);
        return ResponseEntity.noContent().build();
    }

}
