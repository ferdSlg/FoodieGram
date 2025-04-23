package com.ferd.foodiegram.model;

public class Publicacion {
    private String id;
    private String idUsuario;
    private String nombreUsuario;
    private String descripcion;
    private String urlFotoComida;
    private long fecha;

    public Publicacion() {
    } // Necesario para Firebase

    public Publicacion(String idUsuario, String nombreUsuario, String descripcion, String urlFotoComida, long fecha) {
        this.idUsuario = idUsuario;
        this.nombreUsuario = nombreUsuario;
        this.descripcion = descripcion;
        this.urlFotoComida = urlFotoComida;
        this.fecha = fecha;
    }

    // Getters y setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(String idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getUrlFotoComida() {
        return urlFotoComida;
    }

    public void setUrlFotoComida(String urlFotoComida) {
        this.urlFotoComida = urlFotoComida;
    }

    public long getFecha() {
        return fecha;
    }

    public void setFecha(long fecha) {
        this.fecha = fecha;
    }
}
