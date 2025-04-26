package com.ferd.foodiegram.data.repositorios;

import androidx.lifecycle.MutableLiveData;

import com.ferd.foodiegram.model.Usuario;
import com.google.firebase.auth.FirebaseAuth;
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
                        error.setValue("Error en el inicio de sesión: " + task.getException().getMessage());
                    }
                });
    }
}
