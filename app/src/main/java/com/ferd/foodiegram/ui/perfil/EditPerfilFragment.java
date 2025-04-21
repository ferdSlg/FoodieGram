package com.ferd.foodiegram.ui.perfil;

import android.Manifest;
import android.app.Activity;
import android.content.*;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.*;
import android.widget.*;
import androidx.activity.result.*;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.bumptech.glide.Glide;
import com.ferd.foodiegram.R;
import com.ferd.foodiegram.utilidades.Resource;
import com.ferd.foodiegram.viewmodel.EditPerfilViewModel;
import com.ferd.foodiegram.viewmodel.PerfilViewModel;
import com.google.android.material.button.MaterialButton;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class EditPerfilFragment extends Fragment {
    private ImageView imgPerfilEdit;
    private EditText edtCorreo, edtNombre, edtBio, edtPassword;
    private Button btnGuardar;
    private Uri nuevaUri;

    private PerfilViewModel perfilVm;
    private EditPerfilViewModel editVm;

    private ActivityResultLauncher<Intent> galeriaLauncher;
    private ActivityResultLauncher<Uri> camaraLauncher;
    private ActivityResultLauncher<String> permisoCamaraLauncher;

    @Override
    public View onCreateView(@NonNull LayoutInflater inf, ViewGroup c, Bundle b) {
        View v = inf.inflate(R.layout.fragment_edit_perfil, c, false);

        // 1) Bind vistas
        imgPerfilEdit = v.findViewById(R.id.imgPerfilEdit);
        edtCorreo     = v.findViewById(R.id.edtCorreo);
        edtNombre     = v.findViewById(R.id.edtNombre);
        edtBio        = v.findViewById(R.id.edtBio);
        edtPassword   = v.findViewById(R.id.edtPassword);
        btnGuardar    = v.findViewById(R.id.btnGuardarPerfil);

        // 2) ViewModels
        perfilVm = new ViewModelProvider(this).get(PerfilViewModel.class);
        editVm   = new ViewModelProvider(this).get(EditPerfilViewModel.class);

        // 3) Precarga datos actuales
        perfilVm.getUsuario().observe(getViewLifecycleOwner(), u -> {
            if (u == null) return;
            edtCorreo.setText(u.getCorreo());
            edtNombre.setText(u.getNombre());
            edtBio   .setText(u.getBio());
            Glide.with(this)
                    .load(u.getUrlFotoPerfil())
                    .placeholder(R.drawable.user)
                    .into(imgPerfilEdit);
        });

        // 4) Resultado de la actualización
        editVm.getResultado().observe(getViewLifecycleOwner(), res -> {
            if (res == null) return;
            switch (res.status) {
                case LOADING:
                    btnGuardar.setEnabled(false);
                    break;
                case SUCCESS:
                    Toast.makeText(getContext(),
                            "Perfil actualizado", Toast.LENGTH_SHORT).show();
                    requireActivity().onBackPressed();
                    break;
                case ERROR:
                    btnGuardar.setEnabled(true);
                    Toast.makeText(getContext(),
                            "Error: " + res.message, Toast.LENGTH_LONG).show();
                    break;
            }
        });

        // 5) Configura pickers
        configurarPickers();

        // 6) Guardar cambios
        btnGuardar.setOnClickListener(x -> {
            String email    = edtCorreo.getText().toString().trim();
            String nombre   = edtNombre.getText().toString().trim();
            String bio      = edtBio.getText().toString().trim();
            String password = edtPassword.getText().toString(); // puede quedar vacío
            File fotoFile = (nuevaUri != null)
                    ? createTempFileFromUri(nuevaUri)
                    : null;

            editVm.actualizar(email, password, nombre, bio, fotoFile);
        });

        return v;
    }

    private void configurarPickers() {
        galeriaLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), res -> {
                    if (res.getResultCode() == Activity.RESULT_OK && res.getData() != null) {
                        nuevaUri = res.getData().getData();
                        imgPerfilEdit.setImageURI(nuevaUri);
                    }
                }
        );
        camaraLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(), ok -> {
                    if (ok && nuevaUri != null) imgPerfilEdit.setImageURI(nuevaUri);
                }
        );
        permisoCamaraLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(), granted -> {
                    if (granted) launchCamara();
                    else Toast.makeText(getContext(),
                            "Permiso cámara denegado", Toast.LENGTH_SHORT).show();
                }
        );
        imgPerfilEdit.setOnClickListener(v -> abrirGaleria());
    }

    private void abrirGaleria() {
        Intent i = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galeriaLauncher.launch(i);
    }

    private void launchCamara() {
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            ContentValues cv = new ContentValues();
            cv.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            nuevaUri = requireContext().getContentResolver()
                    .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv);
            camaraLauncher.launch(nuevaUri);
        } else {
            permisoCamaraLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private File createTempFileFromUri(Uri uri) {
        File file = new File(requireContext().getCacheDir(),
                "perfil_" + System.currentTimeMillis());
        try (InputStream in = requireContext().getContentResolver().openInputStream(uri);
             OutputStream out = new FileOutputStream(file)) {
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) out.write(buf, 0, len);
        } catch (IOException ignored) {}
        return file;
    }
}