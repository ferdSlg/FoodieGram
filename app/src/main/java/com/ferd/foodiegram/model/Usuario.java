package com.ferd.foodiegram.model;

import java.util.List;

public class Usuario {
    private String id;
    private String nombre;
    private String correo;
    private String urlFotoPerfil;
    private String bio;
    private List<String> seguidores;
    private List<String> seguidos;

    public Usuario() {
    } // Obligatorio para Firebase

    public Usuario(String nombre, String correo) {
        this.nombre = nombre;
        this.correo = correo;
        this.bio = "";
        this.urlFotoPerfil = "";
        this.seguidores = List.of();
        this.seguidos = List.of();
    }

    // Getters y Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getUrlFotoPerfil() {
        return urlFotoPerfil;
    }

    public void setUrlFotoPerfil(String urlFotoPerfil) {
        this.urlFotoPerfil = urlFotoPerfil;
    }

    public List<String> getSeguidores() {
        return seguidores;
    }

    public void setSeguidores(List<String> seguidores) {
        this.seguidores = seguidores;
    }

    public List<String> getSeguidos() {
        return seguidos;
    }

    public void setSeguidos(List<String> seguidos) {
        this.seguidos = seguidos;
    }
}
