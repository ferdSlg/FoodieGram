package com.ferd.foodiegram.ui;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.ferd.foodiegram.R;
import com.ferd.foodiegram.ui.home.HomeFragment;
import com.ferd.foodiegram.viewmodel.RegistroViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;


public class RegistroFragment extends Fragment {

    private EditText editNombre, editCorreo, editContrasena, editConfirmarContrasena;
    private Button botonRegistrar,botonIrLogin;
    private RegistroViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View vista = inflater.inflate(R.layout.fragment_registro, container, false);

        editNombre = vista.findViewById(R.id.editNombre);
        editCorreo = vista.findViewById(R.id.editCorreoRegistro);
        editContrasena = vista.findViewById(R.id.editContrasenaRegistro);
        editConfirmarContrasena = vista.findViewById(R.id.editConfirContrasenaRegistro);
        botonRegistrar = vista.findViewById(R.id.botonRegistrar);
        botonIrLogin = vista.findViewById(R.id.botonIrLogin);

        // Inicializar ViewModel
        viewModel = new ViewModelProvider(this).get(RegistroViewModel.class);

        // Observadores
        viewModel.registroExitoso.observe(getViewLifecycleOwner(), exito -> {
            if (exito) {
                Toast.makeText(getContext(), "Usuario registrado correctamente", Toast.LENGTH_SHORT).show();
                requireActivity()
                        .getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new HomeFragment())
                        // .addToBackStack(null)
                        .commit();
            }
        });

        viewModel.mensajeError.observe(getViewLifecycleOwner(), error -> {
            Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
        });

        botonRegistrar.setOnClickListener(v -> registrarUsuario());
        botonIrLogin.setOnClickListener(v -> irLogin());

        return vista;
    }

    private void registrarUsuario() {
        String nombre = editNombre.getText().toString().trim();
        String correo = editCorreo.getText().toString().trim();
        String contrasena = editContrasena.getText().toString().trim();
        String confirmarContrasena = editConfirmarContrasena.getText().toString().trim();

        if (TextUtils.isEmpty(nombre) || TextUtils.isEmpty(correo) || TextUtils.isEmpty(contrasena) || TextUtils.isEmpty(confirmarContrasena)) {
            Toast.makeText(getContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!contrasena.equals(confirmarContrasena)) {
            Toast.makeText(getContext(), "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
            return;
        }

        viewModel.registrar(nombre, correo, contrasena);
    }

    private void irLogin() {
        getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new LoginFragment())
                .addToBackStack(null)
                .commit();
    }
}