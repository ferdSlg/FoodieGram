package com.ferd.foodiegram.utilidades;

public class Resource<T> {
    public enum Status {
        LOADING,
        SUCCESS,
        ERROR
    }

    public final Status status;
    public final T data;
    public final String message;

    private Resource(Status status, T data, String message) {
        this.status = status;
        this.data = data;
        this.message = message;
    }

    /**
     * Crea un Resource en estado de carga.
     */
    public static <T> Resource<T> loading() {
        return new Resource<>(Status.LOADING, null, null);
    }

    /**
     * Crea un Resource en estado de éxito con datos.
     * @param data El dato resultado.
     */
    public static <T> Resource<T> success(T data) {
        return new Resource<>(Status.SUCCESS, data, null);
    }

    /**
     * Crea un Resource en estado de error con mensaje.
     * @param message Descripción del error.
     */
    public static <T> Resource<T> error(String message) {
        return new Resource<>(Status.ERROR, null, message);
    }

    @Override
    public String toString() {
        return "Resource{" +
                "status=" + status +
                ", data=" + data +
                ", message='" + message + '\'' +
                '}';
    }
}
