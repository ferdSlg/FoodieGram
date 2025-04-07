package com.ferd.foodiegram.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.ferd.foodiegram.data.repositorios.AuthRepositorio;

public class LoginViewModel extends ViewModel {

    private final AuthRepositorio repositorio;

    public MutableLiveData<Boolean> loginExitoso = new MutableLiveData<>();
    public MutableLiveData<String> mensajeError = new MutableLiveData<>();

    public LoginViewModel() {
        repositorio = new AuthRepositorio();
    }

    public void iniciarSesion(String correo, String contrasena) {
        repositorio.iniciarSesion(correo, contrasena, loginExitoso, mensajeError);
    }
}