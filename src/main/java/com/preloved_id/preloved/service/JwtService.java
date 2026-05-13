package com.preloved_id.preloved.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    // key immutable
    // lebih aman daripada field mutable
    private final SecretKey key;

    private final long expiration;

    // constructor injection
    // dependency jelas dan aman
    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration}") long expiration
    ) {

        this.key = Keys.hmacShaKeyFor(
                secret.getBytes(StandardCharsets.UTF_8)
        );

        this.expiration = expiration;
    }

    // generate JWT token
    public String generateToken(String email, String role) {

        return Jwts.builder()
                .subject(email)

                // simpan role di JWT
                // agar tidak query DB terus
                .claim("role", role)

                .issuedAt(new Date())

                .expiration(
                        new Date(
                                System.currentTimeMillis() + expiration
                        )
                )

                .signWith(key)
                .compact();
    }

    // extract semua claims
    public Claims extractClaims(String token) {

        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // extract email dari token
    public String extractEmail(String token) {
        return extractClaims(token).getSubject();
    }

    // validasi token
    public boolean isTokenValid(String token) {

        try {
            extractClaims(token);
            return true;

        } catch (Exception e) {
            return false;
        }
    }
}