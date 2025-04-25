package com.ferd.foodiegram.ui.perfil;

import android.net.Uri;
import android.os.Bundle;
import android.view.*;
import android.webkit.MimeTypeMap;
import android.widget.*;

import androidx.activity.result.*;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.bumptech.glide.Glide;
import com.ferd.foodiegram.R;
import com.ferd.foodiegram.databinding.FragmentEditPerfilBinding;
import com.ferd.foodiegram.viewmodel.EditPerfilViewModel;
import com.ferd.foodiegram.viewmodel.PerfilViewModel;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EditPerfilFragment extends Fragment {
    private FragmentEditPerfilBinding binding;
    private Uri nuevaUri;
    private PerfilViewModel perfilVm;
    private EditPerfilViewModel editVm;
    private ActivityResultLauncher<String> pickImageLauncher;

    @Override
    public View onCreateView(@NonNull LayoutInflater inf, ViewGroup c, Bundle b) {
        binding = FragmentEditPerfilBinding.inflate(inf, c, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        perfilVm = new ViewModelProvider(this).get(PerfilViewModel.class);
        editVm = new ViewModelProvider(this).get(EditPerfilViewModel.class);

        // Precarga datos
        perfilVm.getUsuario().observe(getViewLifecycleOwner(), u -> {
            if (u == null) return;
            binding.edtCorreo.setText(u.getCorreo());
            binding.edtNombre.setText(u.getNombre());
            binding.edtBio.setText(u.getBio());
            Glide.with(this)
                    .load(u.getUrlFotoPerfil())
                    .placeholder(R.drawable.user)
                    .into(binding.imgPerfilEdit);
        });

        // Observa resultado de guardado
        editVm.getResultado().observe(getViewLifecycleOwner(), res -> {
            if (res == null) return;
            switch (res.status) {
                case LOADING:
                    binding.btnGuardarPerfil.setEnabled(false);
                    break;
                case SUCCESS:
                    Toast.makeText(getContext(),
                            "Perfil actualizado", Toast.LENGTH_SHORT).show();
                    NavHostFragment.findNavController(this).popBackStack();
                    break;
                case ERROR:
                    binding.btnGuardarPerfil.setEnabled(true);
                    Toast.makeText(getContext(),
                            "Error: " + res.message, Toast.LENGTH_LONG).show();
                    break;
            }
        });

        configurarGaleriaPicker();
        binding.btnEditFotoPerfil.setOnClickListener(x -> pickImage());
        binding.btnGuardarPerfil.setOnClickListener(x -> {
            String email = binding.edtCorreo.getText().toString().trim();
            String nombre = binding.edtNombre.getText().toString().trim();
            String bio = binding.edtBio.getText().toString().trim();
            String password = binding.edtPassword.getText().toString().trim();
            File fotoFile = (nuevaUri != null)
                    ? createTempFileFromUri(nuevaUri)
                    : null;
            // Lanza la actualización (incluye subida de foto si fotoFile != null)
            editVm.actualizar(email, password, nombre, bio, fotoFile);
        });
    }

    //Método para configurar el lanzador de galería
    private void configurarGaleriaPicker() {
        // Sólo picker de galería: GetContent dispara un selector de tipo MIME
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        nuevaUri = uri;
                        binding.imgPerfilEdit.setImageURI(nuevaUri);
                    }
                }
        );
    }

    //Método para lanzar el lanzador de galería
    private void pickImage() {
        // Filtramos por imágenes
        pickImageLauncher.launch("image/*");
    }

    //Método para crear un archivo temporal a partir de una URI
    private File createTempFileFromUri(Uri uri) {
        String mime = requireContext().getContentResolver().getType(uri);
        String ext = ".jpg";
        if ("image/png".equals(mime)) ext = ".png";
        else if ("image/jpeg".equals(mime)) ext = ".jpg";
        else {
            String guessed = MimeTypeMap.getSingleton().getExtensionFromMimeType(mime);
            if (guessed != null) ext = "." + guessed;
        }

        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File file = new File(requireContext().getCacheDir(), "perfil_" + timestamp + ext);

        try (InputStream in = requireContext().getContentResolver().openInputStream(uri);
             OutputStream out = new FileOutputStream(file)) {
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) out.write(buf, 0, len);
        } catch (IOException ignored) {
        }
        return file;
    }
}