package com.preloved_id.preloved.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

//entity digunakan untuk struktur program untuk match dengan db
@Entity
public class Profile {

    @Id
    private String id;

    private String alamat;

    private String noHp;

    private String foto;

    public Profile() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAlamat() {
        return alamat;
    }

    public void setAlamat(String alamat) {
        this.alamat = alamat;
    }

    public String getNoHp() {
        return noHp;
    }

    public void setNoHp(String noHp) {
        this.noHp = noHp;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }
}