package com.ferd.foodiegram.data.repositorios;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.ferd.foodiegram.model.Usuario;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.firestore.FirebaseFirestore;

public class AuthRepository {

    private final FirebaseAuth auth;
    private final FirebaseFirestore firestore;

    public AuthRepository() {
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
    }

    public void registrarUsuario(Usuario usuario, String contrasena, MutableLiveData<Boolean> exito, MutableLiveData<String> error) {
        auth.createUserWithEmailAndPassword(usuario.getCorreo(), contrasena)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String uid = auth.getCurrentUser().getUid();
                        firestore.collection("usuarios").document(uid).set(usuario)
                                .addOnSuccessListener(unused -> exito.postValue(true))
                                .addOnFailureListener(e -> error.setValue("Error al guardar en Firestore"));
                    } else {
                        error.setValue("Error en autenticación: " + task.getException().getMessage());
                    }
                });
    }

    public void iniciarSesion(String correo, String contrasena, MutableLiveData<Boolean> exito, MutableLiveData<String> error) {
        auth.signInWithEmailAndPassword(correo, contrasena)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        exito.setValue(true);
                    } else {
                        Exception exception = task.getException();
                        if (exception instanceof FirebaseAuthException) {
                            FirebaseAuthException authException = (FirebaseAuthException) exception;
                            String errorCode = authException.getErrorCode() != null ? authException.getErrorCode() : "";
                            String message = authException.getMessage() != null ? authException.getMessage() : "";

                            switch (errorCode) {
                                case "user-not-found":
                                    error.setValue("El correo no está registrado.");
                                    break;
                                case "wrong-password":
                                    error.setValue("La contraseña es incorrecta.");
                                    break;
                                case "invalid-email":
                                    error.setValue("El formato del correo no es válido.");
                                    break;
                                case "user-disabled":
                                    error.setValue("Tu cuenta ha sido deshabilitada.");
                                    break;
                                case "too-many-requests":
                                    error.setValue("Demasiados intentos fallidos. Intenta de nuevo más tarde.");
                                    break;
                                case "operation-not-allowed":
                                    error.setValue("El inicio de sesión con correo y contraseña no está habilitado.");
                                    break;
                                case "network-request-failed":
                                    error.setValue("Error de red. Verifica tu conexión.");
                                    break;
                                default:
                                    if (message.contains("auth credential is incorrect")) {
                                        error.setValue("Correo o contraseña incorrectos.");
                                    } else if (message.contains("password is invalid") || message.contains("user does not have a password")) {
                                        error.setValue("Contraseña incorrecta o el usuario no tiene contraseña.");
                                    } else if (message.contains("no user record")) {
                                        error.setValue("El correo no está registrado.");
                                    } else if (message.contains("badly formatted")) {
                                        error.setValue("El formato del correo no es válido.");
                                    } else if (message.contains("has expired")) {
                                        error.setValue("La sesión ha expirado. Intenta de nuevo.");
                                    } else {
                                        error.setValue("Error desconocido: " + message);
                                    }
                                    break;
                            }
                        } else {
                            error.setValue("Error inesperado: " + (exception != null ? exception.getMessage() : "desconocido"));
                        }
                    }
                });
    }
}
