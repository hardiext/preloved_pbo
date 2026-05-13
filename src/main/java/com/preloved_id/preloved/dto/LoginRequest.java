package com.preloved_id.preloved.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

//request login
public class LoginRequest {


     //validasi email tidak boleh kosong    
    @NotBlank(message = "email tidak boleh kosong")
    @Email(message = "email harus valid")
    private String email;
    
    @Size(min = 8, message = "password min 8")
    private String password;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}