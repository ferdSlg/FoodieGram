package com.ferd.foodiegram.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.ferd.foodiegram.data.repositorios.AuthRepository;
import com.ferd.foodiegram.model.Usuario;

public class RegistroViewModel extends ViewModel {

    private final AuthRepository repositorio;

    public MutableLiveData<Boolean> registroExitoso = new MutableLiveData<>();
    public MutableLiveData<String> mensajeError = new MutableLiveData<>();

    public RegistroViewModel() {
        repositorio = new AuthRepository();
    }

    public void registrar(String nombre, String correo, String contrasena) {
        Usuario usuario = new Usuario(nombre, correo);
        repositorio.registrarUsuario(usuario, contrasena, registroExitoso, mensajeError);
    }
}
