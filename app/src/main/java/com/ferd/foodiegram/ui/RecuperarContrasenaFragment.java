package com.ferd.foodiegram.ui;

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

import com.ferd.foodiegram.R;
import com.ferd.foodiegram.databinding.FragmentRecuperarContrasenaBinding;
import com.ferd.foodiegram.viewmodel.RecuperarViewModel;

public class RecuperarContrasenaFragment extends Fragment {
    private FragmentRecuperarContrasenaBinding binding;
    private RecuperarViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentRecuperarContrasenaBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(RecuperarViewModel.class);

        viewModel.correoEnviado.observe(getViewLifecycleOwner(), enviado -> {
            if (enviado) {
                Toast.makeText(getContext(), "Correo de recuperación enviado", Toast.LENGTH_LONG).show();
                irLogin();
            }
        });
        viewModel.mensajeError.observe(getViewLifecycleOwner(), error -> Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show());
        binding.btnEnviarRecuperacion.setOnClickListener(v -> recuperar());
        binding.botonIrLoginRecu.setOnClickListener(v -> irLogin());

    }

    // Método para recuperar la contraseña
    private void recuperar() {
        String correo = binding.editCorreoRecuperacion.getText().toString().trim();

        if (TextUtils.isEmpty(correo)) {
            Toast.makeText(getContext(), "Introduce tu correo", Toast.LENGTH_SHORT).show();
            return;
        }

        viewModel.enviarCorreoRecuperacion(correo);
    }

    // Método para ir a la pantalla de inicio de sesión
    private void irLogin() {
        getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container_auth, new LoginFragment())
                .addToBackStack(null)
                .commit();
    }
}