package com.ferd.foodiegram.ui.home;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.ferd.foodiegram.R;
import com.ferd.foodiegram.data.supabase.SupabaseClient;
import com.ferd.foodiegram.data.supabase.SupabaseStorageApi;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class CrearPublicacionFragment extends Fragment {

    private ImageView imagenSeleccionada;
    private EditText editDescripcion;
    private Uri uriImagen;

    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    // --- Supabase ---
    private static final String API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InVvY3JqdmdqcWN6ZGZ5aWR5b29nIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDUwOTg1ODksImV4cCI6MjA2MDY3NDU4OX0.eFt4qe2OKmiekG3azM_sy58H_Ypf1rKQTXMIi8Xs4dU";
    private final SupabaseStorageApi storageApi = SupabaseClient
            .getClient()
            .create(SupabaseStorageApi.class);
    // ---------------

    private ActivityResultLauncher<Intent> galeriaLauncher;
    private ActivityResultLauncher<Uri> camaraLauncher;
    private final ActivityResultLauncher<String> permisoCamaraLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    isGranted -> {
                        if (isGranted) lanzarCamara();
                        else Toast.makeText(getContext(),
                                "Permiso de cámara denegado",
                                Toast.LENGTH_SHORT).show();
                    }
            );

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View vista = inflater.inflate(
                R.layout.fragment_crear_publicacion,
                container,
                false
        );

        imagenSeleccionada = vista.findViewById(R.id.imagenSeleccionada);
        editDescripcion    = vista.findViewById(R.id.editDescripcion);
        Button botonGaleria  = vista.findViewById(R.id.botonGaleria);
        Button botonCamara   = vista.findViewById(R.id.botonCamara);
        Button botonPublicar = vista.findViewById(R.id.botonPublicar);

        configurarPickers();

        botonGaleria.setOnClickListener(v -> abrirGaleria());
        botonCamara .setOnClickListener(v -> abrirCamara());
        botonPublicar.setOnClickListener(v -> subirPublicacion());

        return vista;
    }

    private void configurarPickers() {
        galeriaLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK
                            && result.getData() != null) {
                        uriImagen = result.getData().getData();
                        imagenSeleccionada.setImageURI(uriImagen);
                    }
                }
        );

        camaraLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                exitoso -> {
                    if (exitoso && uriImagen != null) {
                        imagenSeleccionada.setImageURI(uriImagen);
                    }
                }
        );
    }

    private void abrirGaleria() {
        Intent intent = new Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        );
        galeriaLauncher.launch(intent);
    }

    private void abrirCamara() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        ) {
            lanzarCamara();
        } else {
            permisoCamaraLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void subirPublicacion() {
        String descripcion = editDescripcion.getText()
                .toString().trim();

        if (uriImagen == null || descripcion.isEmpty()) {
            Toast.makeText(getContext(),
                    "Debes seleccionar una imagen y escribir una descripción",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // 1) Convertir Uri a File temporal
        File imageFile = createTempFileFromUri(uriImagen);

        // 2) Preparar MultipartBody.Part
        RequestBody reqFile = RequestBody.create(
                MediaType.parse("image/*"),
                imageFile
        );
        MultipartBody.Part body = MultipartBody.Part.createFormData(
                "file",
                imageFile.getName(),
                reqFile
        );

        // 3) Llamada a Supabase
        Call<Void> call = storageApi.uploadImage(
                "Bearer " + API_KEY,
                "fotos",
                imageFile.getName(),
                body
        );

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(
                    Call<Void> call,
                    Response<Void> response
            ) {
                if (response.isSuccessful()) {
                    // URL pública = petición original
                    String fileUrl = response.raw()
                            .request()
                            .url()
                            .toString();

                    // 4) Guardar en Firestore
                    Map<String,Object> datos = new HashMap<>();
                    datos.put("urlFotoComida", fileUrl);
                    datos.put("descripcion", descripcion);
                    datos.put("fecha", System.currentTimeMillis());
                    datos.put("idUsuario", auth.getCurrentUser().getUid());
                    datos.put("nombreUsuario",
                            auth.getCurrentUser().getEmail());

                    firestore.collection("publicaciones")
                            .add(datos)
                            .addOnSuccessListener(docRef -> Toast
                                    .makeText(getContext(),
                                            "¡Publicación subida!",
                                            Toast.LENGTH_SHORT)
                                    .show()
                            )
                            .addOnFailureListener(e -> Toast
                                    .makeText(getContext(),
                                            "Error al guardar en Firestore",
                                            Toast.LENGTH_SHORT)
                                    .show()
                            );
                } else {
                    Toast.makeText(getContext(),
                            "Error al subir imagen a Supabase",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(getContext(),
                        "Fallo de red: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private File createTempFileFromUri(Uri uri) {
        File file = new File(
                requireContext().getCacheDir(),
                "upload_" + System.currentTimeMillis()
        );
        try (
                InputStream in = requireContext()
                        .getContentResolver()
                        .openInputStream(uri);
                OutputStream out = new FileOutputStream(file)
        ) {
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    private void lanzarCamara() {
        String nombre = new SimpleDateFormat(
                "yyyyMMdd_HHmmss",
                Locale.getDefault()
        ).format(new Date());
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "IMG_" + nombre);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");

        uriImagen = requireContext()
                .getContentResolver()
                .insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        values
                );
        camaraLauncher.launch(uriImagen);
    }
}
