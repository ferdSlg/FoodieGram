package com.ferd.foodiegram.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;

public class RecuperarViewModel extends ViewModel {

    private final MutableLiveData<Boolean> _correoEnviado = new MutableLiveData<>();
    private final MutableLiveData<String> _mensajeError = new MutableLiveData<>();

    public LiveData<Boolean> correoEnviado = _correoEnviado;
    public LiveData<String> mensajeError = _mensajeError;

    public void enviarCorreoRecuperacion(String correo) {
        FirebaseAuth.getInstance().sendPasswordResetEmail(correo)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        _correoEnviado.setValue(true);
                    } else {
                        _mensajeError.setValue("No se pudo enviar el correo de recuperación");
                    }
                });
    }
}
