package com.ferd.foodiegram.ui;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.ferd.foodiegram.R;
import com.ferd.foodiegram.ui.home.HomeFragment;
import com.ferd.foodiegram.viewmodel.LoginViewModel;
import com.google.firebase.auth.FirebaseAuth;


public class LoginFragment extends Fragment {

    private EditText campoCorreo, campoContrasena;
    private Button botonIniciarSesion, botonIrARegistro;
    private LoginViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View vista = inflater.inflate(R.layout.fragment_login, container, false);

        campoCorreo = vista.findViewById(R.id.editCorreo);
        campoContrasena = vista.findViewById(R.id.editContrasena);
        botonIniciarSesion = vista.findViewById(R.id.botonLogin);
        botonIrARegistro = vista.findViewById(R.id.botonIrRegistro);

        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        viewModel.loginExitoso.observe(getViewLifecycleOwner(), exito -> {
            if (exito) {
                Toast.makeText(getContext(), "Bienvenido", Toast.LENGTH_SHORT).show();
                getParentFragmentManager()
                        .beginTransaction()
                        .replace(R.id.main, new HomeFragment())
                        .commitNow();
            }
        });

        viewModel.mensajeError.observe(getViewLifecycleOwner(), error ->
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show()
        );

        botonIniciarSesion.setOnClickListener(v -> iniciarSesion());
        botonIrARegistro.setOnClickListener(v -> irARegistro());

        return vista;
    }

    private void iniciarSesion() {
        String correo = campoCorreo.getText().toString().trim();
        String contrasena = campoContrasena.getText().toString().trim();

        if (TextUtils.isEmpty(correo) || TextUtils.isEmpty(contrasena)) {
            Toast.makeText(getContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        viewModel.iniciarSesion(correo, contrasena);
    }

    private void irARegistro() {
        getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.main, new RegistroFragment())
                .addToBackStack(null)
                .commit();
    }
}