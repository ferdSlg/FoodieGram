package com.ferd.foodiegram.ui.perfil;

import android.Manifest;
import android.app.Activity;
import android.content.*;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.*;
import android.webkit.MimeTypeMap;
import android.widget.*;

import androidx.activity.result.*;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.bumptech.glide.Glide;
import com.ferd.foodiegram.R;
import com.ferd.foodiegram.utilidades.Resource;
import com.ferd.foodiegram.viewmodel.EditPerfilViewModel;
import com.ferd.foodiegram.viewmodel.PerfilViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class EditPerfilFragment extends Fragment {
    private ImageView imgPerfilEdit;
    private FloatingActionButton btnEditFotoPerfil;
    private EditText edtCorreo, edtNombre, edtBio, edtPassword;
    private Button btnGuardar;
    private Uri nuevaUri;

    private PerfilViewModel perfilVm;
    private EditPerfilViewModel editVm;

    // Nuevo: lanzador sólo para galería
    private ActivityResultLauncher<String> pickImageLauncher;

    @Override
    public View onCreateView(@NonNull LayoutInflater inf, ViewGroup c, Bundle b) {
        View v = inf.inflate(R.layout.fragment_edit_perfil, c, false);

        imgPerfilEdit       = v.findViewById(R.id.imgPerfilEdit);
        btnEditFotoPerfil   = v.findViewById(R.id.btnEditFotoPerfil);
        edtCorreo           = v.findViewById(R.id.edtCorreo);
        edtNombre           = v.findViewById(R.id.edtNombre);
        edtBio              = v.findViewById(R.id.edtBio);
        edtPassword         = v.findViewById(R.id.edtPassword);
        btnGuardar          = v.findViewById(R.id.btnGuardarPerfil);

        perfilVm = new ViewModelProvider(this).get(PerfilViewModel.class);
        editVm   = new ViewModelProvider(this).get(EditPerfilViewModel.class);

        // Precarga datos
        perfilVm.getUsuario().observe(getViewLifecycleOwner(), u -> {
            if (u == null) return;
            edtCorreo.setText(u.getCorreo());
            edtNombre.setText(u.getNombre());
            edtBio.setText(u.getBio());
            Glide.with(this)
                    .load(u.getUrlFotoPerfil())
                    .placeholder(R.drawable.user)
                    .into(imgPerfilEdit);
        });

        // Observa resultado de guardado
        editVm.getResultado().observe(getViewLifecycleOwner(), res -> {
            if (res == null) return;
            switch (res.status) {
                case LOADING:
                    btnGuardar.setEnabled(false);
                    break;
                case SUCCESS:
                    Toast.makeText(getContext(),
                            "Perfil actualizado", Toast.LENGTH_SHORT).show();
                    NavHostFragment.findNavController(this).popBackStack();
                    break;
                case ERROR:
                    btnGuardar.setEnabled(true);
                    Toast.makeText(getContext(),
                            "Error: " + res.message, Toast.LENGTH_LONG).show();
                    break;
            }
        });

        configurarGaleriaPicker();

        // Al pulsar el icono de cámara, abrimos galería
        btnEditFotoPerfil.setOnClickListener(x -> pickImage());
        // (Opcional) también puedes ponerlo en la propia imagen:
        //imgPerfilEdit.setOnClickListener(x -> pickImage());

        btnGuardar.setOnClickListener(x -> {
            String email    = edtCorreo.getText().toString().trim();
            String nombre   = edtNombre.getText().toString().trim();
            String bio      = edtBio.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();
            File fotoFile   = (nuevaUri != null)
                    ? createTempFileFromUri(nuevaUri)
                    : null;
            // Lanza la actualización (incluye subida de foto si fotoFile != null)
            editVm.actualizar(email, password, nombre, bio, fotoFile);
        });

        return v;
    }

    private void configurarGaleriaPicker() {
        // Sólo picker de galería: GetContent dispara un selector de tipo MIME
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        nuevaUri = uri;
                        imgPerfilEdit.setImageURI(nuevaUri);
                    }
                }
        );
    }

    private void pickImage() {
        // Filtramos por imágenes
        pickImageLauncher.launch("image/*");
    }

    // Este método queda igual que antes
    private File createTempFileFromUri(Uri uri) {
        String mime = requireContext().getContentResolver().getType(uri);
        String ext = ".jpg";
        if ("image/png".equals(mime)) ext = ".png";
        else if ("image/jpeg".equals(mime)) ext = ".jpg";
        else {
            String guessed = MimeTypeMap.getSingleton()
                    .getExtensionFromMimeType(mime);
            if (guessed != null) ext = "." + guessed;
        }

        String timestamp = new SimpleDateFormat(
                "yyyyMMdd_HHmmss", Locale.getDefault()
        ).format(new Date());
        File file = new File(requireContext().getCacheDir(),
                "perfil_" + timestamp + ext);

        try (InputStream in = requireContext()
                .getContentResolver().openInputStream(uri);
             OutputStream out = new FileOutputStream(file)) {
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) out.write(buf, 0, len);
        } catch (IOException ignored) { }

        return file;
    }
}