package com.preloved_id.preloved.dto;

import com.preloved_id.preloved.model.Role;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

//request register
public class RegisterRequest {

    //valdiasi nama tidak kosong
    @NotBlank(message = "Nama tidak boleh kosong")
    private String nama;

    //validasi email tidak boleh kosong    
    @NotBlank(message = "email tidak boleh kosong")
    @Email(message = "email harus valid")
    private String email;

    @Size(min = 8, message = "password min 8")
    private String password;
    private Role role;
    private String storeName;

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

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

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }
}