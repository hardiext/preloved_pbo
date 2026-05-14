package com.preloved_id.preloved.dto;

//struktur response yang ingin ditampilkan atau untuk cek di postman
public class AuthResponse {

    // privat variable untuk response nanti
    private String message;
    private String token;
    private String role;

    // constriuctor untuk injec response dari user
    public AuthResponse(String message, String token, String role) {
        this.message = message;
        this.token = token;
        this.role = role;
    }

    // getter setter message
    public String getMessage() {
        return message;
    }

    // getter setter get toke
    public String getToken() {
        return token;
    }

    // getter setter get role
    public String getRole() {
        return role;
    }
}