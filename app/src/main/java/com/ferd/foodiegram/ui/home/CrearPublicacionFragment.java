package com.ferd.foodiegram.ui.home;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.ferd.foodiegram.R;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class CrearPublicacionFragment extends Fragment {

    private static final int REQ_GALERIA = 101;
    private static final int REQ_CAMARA = 102;
    private static final int REQ_PERMISOS = 103;

    private ImageView imagenSeleccionada;
    private Uri uriImagenSeleccionada;
    private String rutaFotoActual;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View vista = inflater.inflate(R.layout.fragment_crear_publicacion, container, false);

        imagenSeleccionada = vista.findViewById(R.id.imagenSeleccionada);
        Button botonGaleria = vista.findViewById(R.id.botonGaleria);
        Button botonCamara = vista.findViewById(R.id.botonCamara);
        Button botonPublicar = vista.findViewById(R.id.botonPublicar);
        EditText editDescripcion = vista.findViewById(R.id.editDescripcion);

        botonGaleria.setOnClickListener(v -> abrirGaleria());
        botonCamara.setOnClickListener(v -> abrirCamara());

        return vista;
    }

    private void abrirGaleria() {
        if (tienePermisos()) {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, REQ_GALERIA);
        } else {
            pedirPermisos();
        }
    }

    private void abrirCamara() {
        if (tienePermisos()) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (intent.resolveActivity(requireActivity().getPackageManager()) != null) {
                File foto = crearArchivoImagen();
                if (foto != null) {
                    uriImagenSeleccionada = FileProvider.getUriForFile(requireContext(),
                            requireContext().getPackageName() + ".fileprovider", foto);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, uriImagenSeleccionada);
                    startActivityForResult(intent, REQ_CAMARA);
                }
            }
        } else {
            pedirPermisos();
        }
    }

    private boolean tienePermisos() {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void pedirPermisos() {
        requestPermissions(new String[]{
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE
        }, REQ_PERMISOS);
    }

    private File crearArchivoImagen() {
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String nombreArchivo = "JPEG_" + timeStamp + "_";
            File directorio = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            File imagen = File.createTempFile(nombreArchivo, ".jpg", directorio);
            rutaFotoActual = imagen.getAbsolutePath();
            return imagen;
        } catch (IOException e) {
            Toast.makeText(getContext(), "Error al crear imagen", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQ_GALERIA && data != null) {
                uriImagenSeleccionada = data.getData();
                imagenSeleccionada.setImageURI(uriImagenSeleccionada);
            } else if (requestCode == REQ_CAMARA) {
                imagenSeleccionada.setImageURI(uriImagenSeleccionada);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permisos, @NonNull int[] resultados) {
        super.onRequestPermissionsResult(requestCode, permisos, resultados);
        if (requestCode == REQ_PERMISOS) {
            if (tienePermisos()) {
                Toast.makeText(getContext(), "Permisos concedidos", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Permisos necesarios no concedidos", Toast.LENGTH_SHORT).show();
            }
        }
    }
}