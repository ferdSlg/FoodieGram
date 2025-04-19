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
import com.ferd.foodiegram.viewmodel.RecuperarViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;


public class RecuperarContrasenaFragment extends Fragment {

    private EditText editCorreo;
    private MaterialButton btnEnviar;
    private Button botonIrLogin;
    private RecuperarViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View vista = inflater.inflate(R.layout.fragment_recuperar_contrasena, container, false);

        editCorreo = vista.findViewById(R.id.editCorreoRecuperacion);
        btnEnviar = vista.findViewById(R.id.btnEnviarRecuperacion);
        botonIrLogin = vista.findViewById(R.id.botonIrLoginRecu);

        viewModel = new ViewModelProvider(this).get(RecuperarViewModel.class);

        viewModel.correoEnviado.observe(getViewLifecycleOwner(), enviado -> {
            if (enviado) {
                Toast.makeText(getContext(), "Correo de recuperación enviado", Toast.LENGTH_LONG).show();
                irLogin();
            }
        });

        viewModel.mensajeError.observe(getViewLifecycleOwner(), error ->
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show()
        );

        btnEnviar.setOnClickListener(v -> recuperar());
        botonIrLogin.setOnClickListener(v -> irLogin());

        return vista;
    }

    private void recuperar() {
        String correo = editCorreo.getText().toString().trim();

        if (TextUtils.isEmpty(correo)) {
            Toast.makeText(getContext(), "Introduce tu correo", Toast.LENGTH_SHORT).show();
            return;
        }

        viewModel.enviarCorreoRecuperacion(correo);
    }

    private void irLogin() {
        getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container_auth, new LoginFragment())
                .addToBackStack(null)
                .commit();
    }
}