package com.preloved_id.preloved.model;

import jakarta.persistence.*;

//entity digunakan untuk struktur program untuk match dengan db
@Entity

// spesifik ke tabel users
@Table(name = "users")

// inheritance join
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class User {

    // menegaskan id
    @Id
    private String id;

    private String nama;

    // validasi bahwa unique email harus unik
    @Column(unique = true)
    private String email;

    private String password;

    //artinya berelasi
    @OneToOne(cascade = CascadeType.ALL)
    //lakukan join colum dengan profile id
    @JoinColumn(name = "profile_id")
    private Profile profile;

    public User() {
    }

    public abstract void displayRole();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }
}