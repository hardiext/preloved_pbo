package com.preloved_id.preloved.controller;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

import com.preloved_id.preloved.dto.AuthResponse;
import com.preloved_id.preloved.dto.LoginRequest;
import com.preloved_id.preloved.dto.RegisterRequest;
import com.preloved_id.preloved.service.AuthService;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin("*")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public AuthResponse register(
            @Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(
            @Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
}