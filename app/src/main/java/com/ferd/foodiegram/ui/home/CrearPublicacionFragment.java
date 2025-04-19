package com.ferd.foodiegram.ui.home;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
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

import com.bumptech.glide.Glide;
import com.ferd.foodiegram.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CrearPublicacionFragment extends Fragment {

    private ImageView imagenSeleccionada;
    private EditText editDescripcion;
    private Uri uriImagen;

    private final FirebaseStorage storage = FirebaseStorage.getInstance();
    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    private ActivityResultLauncher<Intent> galeriaLauncher;
    private ActivityResultLauncher<Uri> camaraLauncher;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View vista = inflater.inflate(R.layout.fragment_crear_publicacion, container, false);

        imagenSeleccionada = vista.findViewById(R.id.imagenSeleccionada);
        editDescripcion = vista.findViewById(R.id.editDescripcion);
        Button botonGaleria = vista.findViewById(R.id.botonGaleria);
        Button botonCamara = vista.findViewById(R.id.botonCamara);
        Button botonPublicar = vista.findViewById(R.id.botonPublicar);

        configurarPickers();

        botonGaleria.setOnClickListener(v -> abrirGaleria());
        botonCamara.setOnClickListener(v -> abrirCamara());
        botonPublicar.setOnClickListener(v -> subirPublicacion());

        return vista;
    }

    private void configurarPickers() {
        galeriaLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        uriImagen = result.getData().getData();
                        imagenSeleccionada.setImageURI(uriImagen);
                    }
                });

        camaraLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                exitoso -> {
                    if (exitoso && uriImagen != null) {
                        imagenSeleccionada.setImageURI(uriImagen);
                    }
                });
    }

    private void abrirGaleria() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galeriaLauncher.launch(intent);
    }

    private final ActivityResultLauncher<String> permisoCamaraLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    lanzarCamara();
                } else {
                    Toast.makeText(getContext(), "Permiso de cámara denegado", Toast.LENGTH_SHORT).show();
                }
            });

    private void abrirCamara() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            lanzarCamara();
        } else {
            permisoCamaraLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void subirPublicacion() {
        String descripcion = editDescripcion.getText().toString().trim();

        if (uriImagen == null || descripcion.isEmpty()) {
            Toast.makeText(getContext(), "Debes seleccionar una imagen y escribir una descripción", Toast.LENGTH_SHORT).show();
            return;
        }

        String nombreArchivo = "publicaciones/" + System.currentTimeMillis() + ".jpg";
        StorageReference ref = storage.getReference().child(nombreArchivo);

        ref.putFile(uriImagen)
                .addOnSuccessListener(taskSnapshot -> ref.getDownloadUrl().addOnSuccessListener(uri -> {
                    guardarEnFirestore(uri.toString(), descripcion);
                }))
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error al subir imagen", Toast.LENGTH_SHORT).show()
                );
    }

    private void guardarEnFirestore(String urlImagen, String descripcion) {
        String uid = auth.getCurrentUser().getUid();
        String nombreUsuario = auth.getCurrentUser().getEmail(); // puedes cambiarlo si tienes nombre guardado

        Map<String, Object> datos = new HashMap<>();
        datos.put("idUsuario", uid);
        datos.put("nombreUsuario", nombreUsuario);
        datos.put("descripcion", descripcion);
        datos.put("urlFotoComida", urlImagen);
        datos.put("fecha", System.currentTimeMillis());

        firestore.collection("publicaciones")
                .add(datos)
                .addOnSuccessListener(documentReference ->
                        Toast.makeText(getContext(), "¡Publicación subida!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error al guardar en Firestore", Toast.LENGTH_SHORT).show());
    }

    private void lanzarCamara() {
        String nombre = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "IMG_" + nombre);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");

        uriImagen = requireContext().getContentResolver()
                .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        camaraLauncher.launch(uriImagen);
    }
}
