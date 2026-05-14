package com.preloved_id.preloved.controller;


import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.preloved_id.preloved.dto.AuthResponse;
import com.preloved_id.preloved.dto.LoginRequest;
import com.preloved_id.preloved.dto.RegisterRequest;
import com.preloved_id.preloved.service.AuthService;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/api/auth")
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
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response) {

        AuthResponse authResponse = authService.login(request);

        org.springframework.http.ResponseCookie jwtCookie = org.springframework.http.ResponseCookie.from(
                "token",
                authResponse.getToken())
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(60 * 60)
                .sameSite("Lax")
                .build();

        org.springframework.http.ResponseCookie roleCookie = org.springframework.http.ResponseCookie.from(
                "role",
                authResponse.getRole())
                .httpOnly(false)
                .secure(false)
                .path("/")
                .maxAge(60 * 60)
                .sameSite("Lax")
                .build();

        response.addHeader(
                org.springframework.http.HttpHeaders.SET_COOKIE,
                jwtCookie.toString());

        response.addHeader(
                org.springframework.http.HttpHeaders.SET_COOKIE,
                roleCookie.toString());

        return authResponse;
    }

    @PostMapping("/logout")
public String logout(HttpServletResponse response) {

    org.springframework.http.ResponseCookie jwtCookie =
            org.springframework.http.ResponseCookie.from("token", "")
                    .httpOnly(true)
                    .secure(false)
                    .path("/")
                    .maxAge(0)
                    .sameSite("Lax")
                    .build();

    org.springframework.http.ResponseCookie roleCookie =
            org.springframework.http.ResponseCookie.from("role", "")
                    .httpOnly(false)
                    .secure(false)
                    .path("/")
                    .maxAge(0)
                    .sameSite("Lax")
                    .build();

    response.addHeader(
            org.springframework.http.HttpHeaders.SET_COOKIE,
            jwtCookie.toString()
    );

    response.addHeader(
            org.springframework.http.HttpHeaders.SET_COOKIE,
            roleCookie.toString()
    );

    return "Logout berhasil";
}
@GetMapping("/me")
public ResponseEntity<?> getCurrentUser() {

    Authentication auth =
            SecurityContextHolder.getContext().getAuthentication();

    if (
            auth == null ||
            auth.getPrincipal().equals("anonymousUser")
    ) {
        return ResponseEntity.status(401).body("Unauthorized");
    }

    Map<String, Object> userData = new HashMap<>();

    userData.put("email", auth.getName());

    userData.put(
            "role",
            auth.getAuthorities()
                    .iterator()
                    .next()
                    .getAuthority()
                    .replace("ROLE_", "")
    );

    return ResponseEntity.ok(userData);
}
}