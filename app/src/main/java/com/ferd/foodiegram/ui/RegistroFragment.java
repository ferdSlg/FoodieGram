package com.ferd.foodiegram.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.ferd.foodiegram.MainActivity;
import com.ferd.foodiegram.R;
import com.ferd.foodiegram.databinding.FragmentRegistroBinding;
import com.ferd.foodiegram.viewmodel.RegistroViewModel;

public class RegistroFragment extends Fragment {
    private FragmentRegistroBinding binding;
    private RegistroViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentRegistroBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Inicializar ViewModel
        viewModel = new ViewModelProvider(this).get(RegistroViewModel.class);

        // Observadores
        viewModel.registroExitoso.observe(getViewLifecycleOwner(), exito -> {
            if (exito) {
                Toast.makeText(getContext(), "Usuario registrado correctamente", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(requireActivity(), MainActivity.class);
                startActivity(i);
                requireActivity().finish();
            }
        });

        viewModel.mensajeError.observe(getViewLifecycleOwner(), error -> {
            Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
        });

        binding.botonRegistrar.setOnClickListener(v -> registrarUsuario());
        binding.botonIrLogin.setOnClickListener(v -> irLogin());

    }

    //Método para registrar usuario
    private void registrarUsuario() {
        String nombre = binding.editNombre.getText().toString().trim();
        String correo = binding.editCorreoRegistro.getText().toString().trim();
        String contrasena = binding.editContrasenaRegistro.getText().toString().trim();
        String confirmarContrasena = binding.editConfirContrasenaRegistro.getText().toString().trim();
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

    //Método para ir a login
    private void irLogin() {
        getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container_auth, new LoginFragment())
                .addToBackStack(null)
                .commit();
    }
}