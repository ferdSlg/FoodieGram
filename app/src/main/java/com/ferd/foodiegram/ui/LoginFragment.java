package com.ferd.foodiegram.ui;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.ferd.foodiegram.MainActivity;
import com.ferd.foodiegram.R;
import com.ferd.foodiegram.ui.home.HomeFragment;
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

    private EditText campoCorreo, campoContrasena;
    private Button botonIniciarSesion, botonIrARegistro, btnRecuperarContrasena;
    private ImageButton botonGoogle;
    private LoginViewModel viewModel;

    private ActivityResultLauncher<Intent> googleSignInLauncher;
    private GoogleSignInClient googleSignInClient;
    private static final int RC_SIGN_IN = 9001;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View vista = inflater.inflate(R.layout.fragment_login, container, false);

        campoCorreo = vista.findViewById(R.id.editCorreo);
        campoContrasena = vista.findViewById(R.id.editContrasena);
        botonIniciarSesion = vista.findViewById(R.id.botonLogin);
        botonIrARegistro = vista.findViewById(R.id.botonIrRegistro);
        botonGoogle= vista.findViewById(R.id.botonGoogle);
        btnRecuperarContrasena = vista.findViewById(R.id.btnRecuperarPass);

        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        viewModel.loginExitoso.observe(getViewLifecycleOwner(), exito -> {
            if (exito) {
                Toast.makeText(getContext(), "Bienvenido", Toast.LENGTH_SHORT).show();
                getParentFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new HomeFragment())
                        .commit();
            }
        });

        viewModel.mensajeError.observe(getViewLifecycleOwner(), error ->
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show()
        );

        botonIniciarSesion.setOnClickListener(v -> iniciarSesion());
        botonIrARegistro.setOnClickListener(v -> irARegistro());

        configurarClienteGoogleSignIn();
        inicializarLauncherGoogleSignIn();
        botonGoogle.setOnClickListener(v -> signInWithGoogle());

        btnRecuperarContrasena.setOnClickListener(v -> {
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new RecuperarContrasenaFragment())
                    .addToBackStack(null)
                    .commit();
        });

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
                .replace(R.id.fragment_container, new RegistroFragment())
                .addToBackStack(null)
                .commit();
    }

    private void configurarClienteGoogleSignIn() {
        // Configurar Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // Usa tu Web client ID
                .requestEmail()
                .build();

        // Inicializar Google Sign-In a partir de la configuración previa
        googleSignInClient = GoogleSignIn.getClient(requireContext(), gso);
    }

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

    private void signInWithGoogle() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        googleSignInLauncher.launch(signInIntent);
    }

    private void gestionarResultadoSignIn(Task<GoogleSignInAccount> task) {
        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            firebaseAuthWithGoogle(account);
        } catch (ApiException e) {
            Toast.makeText(getContext(), "Error al iniciar sesión con Google", Toast.LENGTH_SHORT).show();
        }
    }

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