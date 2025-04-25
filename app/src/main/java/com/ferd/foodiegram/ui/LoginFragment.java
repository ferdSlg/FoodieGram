package com.ferd.foodiegram.ui;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
import com.ferd.foodiegram.databinding.FragmentLoginBinding;
import com.ferd.foodiegram.viewmodel.LoginViewModel;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;


public class LoginFragment extends Fragment {
    private FragmentLoginBinding binding;
    private LoginViewModel viewModel;
    private ActivityResultLauncher<Intent> googleSignInLauncher;
    private GoogleSignInClient googleSignInClient;
    private static final int RC_SIGN_IN = 9001;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);
        viewModel.loginExitoso.observe(getViewLifecycleOwner(), exito -> {
            if (exito) {
                Toast.makeText(getContext(), "Bienvenido", Toast.LENGTH_SHORT).show();
                // Arrancamos MainActivity y cerramos AuthActivity
                Intent i = new Intent(requireActivity(), MainActivity.class);
                startActivity(i);
                requireActivity().finish();
            }
        });
        viewModel.mensajeError.observe(getViewLifecycleOwner(), error ->
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show()
        );
        binding.botonLogin.setOnClickListener(v -> iniciarSesion());
        binding.botonIrRegistro.setOnClickListener(v -> irARegistro());
        configurarClienteGoogleSignIn();
        inicializarLauncherGoogleSignIn();
        binding.botonGoogle.setOnClickListener(v -> signInWithGoogle());
        binding.btnRecuperarPass.setOnClickListener(v -> {
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container_auth, new RecuperarContrasenaFragment())
                    .addToBackStack(null)
                    .commit();
        });
    }

    //Método para iniciar sesión
    private void iniciarSesion() {
        String correo = binding.editCorreo.getText().toString().trim();
        String contrasena = binding.editContrasena.getText().toString().trim();
        if (TextUtils.isEmpty(correo) || TextUtils.isEmpty(contrasena)) {
            Toast.makeText(getContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }
        viewModel.iniciarSesion(correo, contrasena);
    }

    //Método para ir a registro
    private void irARegistro() {
        getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container_auth, new RegistroFragment())
                .addToBackStack(null)
                .commit();
    }

    //Método para configurar el cliente de Google Sign-In
    private void configurarClienteGoogleSignIn() {
        // Configurar Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // Usa el Web client ID
                .requestEmail()
                .build();
        // Inicializar Google Sign-In a partir de la configuración previa
        googleSignInClient = GoogleSignIn.getClient(requireContext(), gso);
    }

    //Método para inicializar el ActivityResultLauncher para manejar la respuesta de Google Sign-In
    private void inicializarLauncherGoogleSignIn() {
        // Inicializar el ActivityResultLauncher para manejar la respuesta de Google Sign-In
        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                        gestionarResultadoSignIn(task);
                    } else {
                        Toast.makeText(getContext(), "Error en el inicio de sesión con Google", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    //Método para iniciar sesión con Google
    private void signInWithGoogle() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        googleSignInLauncher.launch(signInIntent);
    }

    //Método para gestionar el resultado del inicio de sesión con Google
    private void gestionarResultadoSignIn(Task<GoogleSignInAccount> task) {
        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            firebaseAuthWithGoogle(account);
        } catch (ApiException e) {
            Toast.makeText(getContext(), "Error al iniciar sesión con Google", Toast.LENGTH_SHORT).show();
        }
    }

    //Método para autenticar con Firebase a partir de la cuenta de Google
    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(), "Inicio de sesión con Google exitoso", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getContext(), MainActivity.class);
                        startActivity(intent);
                        requireActivity().finish();
                    } else {
                        Toast.makeText(getContext(), "Error al autenticar con Firebase", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}