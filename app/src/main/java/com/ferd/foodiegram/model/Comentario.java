package com.ferd.foodiegram.model;

public class Comentario {
    //private String id;
    private String uidAutor;
    private String texto;
    private long timestamp;
    public Comentario() { } // Para Firestore

    public Comentario(String uidAutor, String texto, long timestamp) {
        this.uidAutor = uidAutor;
        this.texto = texto;
        this.timestamp = timestamp;
    }

    /*public String getId() {
        return id;
    }*/

    /*public void setId(String id) {
        this.id = id;
    }*/

    public String getUidAutor() {
        return uidAutor;
    }

    public void setUidAutor(String uidAutor) {
        this.uidAutor = uidAutor;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
