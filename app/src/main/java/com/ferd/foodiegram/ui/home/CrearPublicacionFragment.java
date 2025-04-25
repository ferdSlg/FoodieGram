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
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.ferd.foodiegram.R;
import com.ferd.foodiegram.data.supabase.SupabaseClient;
import com.ferd.foodiegram.data.supabase.SupabaseStorageApi;
import com.ferd.foodiegram.viewmodel.CrearPublicacionViewModel;
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
    private Button botonGaleria, botonCamara, botonPublicar;
    private Uri uriImagen;

    private ActivityResultLauncher<Intent> galeriaLauncher;
    private ActivityResultLauncher<Uri> camaraLauncher;
    private ActivityResultLauncher<String> permisoCamaraLauncher;

    private CrearPublicacionViewModel viewModel;
    private NavController navController;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View vista = inflater.inflate(
                R.layout.fragment_crear_publicacion,
                container,
                false
        );
        // Vincular vistas
        imagenSeleccionada = vista.findViewById(R.id.imagenSeleccionada);
        editDescripcion = vista.findViewById(R.id.editDescripcion);
        botonGaleria = vista.findViewById(R.id.botonGaleria);
        botonCamara = vista.findViewById(R.id.botonCamara);
        botonPublicar = vista.findViewById(R.id.botonPublicar);

        return vista;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inicializar ViewModel
        viewModel = new ViewModelProvider(this).get(CrearPublicacionViewModel.class);

        // Configurar pickers
        configurarPickers();

        // Observadores
        observarViewModel();

        navController = Navigation.findNavController(view);

        // Listeners de botones
        botonGaleria.setOnClickListener(v -> abrirGaleria());
        botonCamara.setOnClickListener(v -> abrirCamara());
        botonPublicar.setOnClickListener(v -> publicar(navController));

    }

    private void configurarPickers() {
        // Galería
        galeriaLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK
                            && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        viewModel.setImagen(uri);
                    }
                }
        );

        // Cámara
        camaraLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                exitoso -> {
                    if (exitoso && uriImagen != null) {
                        viewModel.setImagen(uriImagen);
                    }
                }
        );

        permisoCamaraLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) lanzarCamara();
                    else Toast.makeText(getContext(),
                                    "Permiso de cámara denegado", Toast.LENGTH_SHORT)
                            .show();
                }
        );
    }

    private void observarViewModel() {
        // Imagen seleccionada
        viewModel.getImagenSeleccionada().observe(
                getViewLifecycleOwner(), uri -> {
                    if (uri != null) {
                        imagenSeleccionada.setImageURI(uri);
                        uriImagen = uri;
                    }
                }
        );

        // Estado de subida
        viewModel.getResultadoSubida().observe(
                getViewLifecycleOwner(), resource -> {
                    if (resource == null) return;
                    switch (resource.status) {
                        case LOADING:
                            botonPublicar.setEnabled(false);
                            // Puedes mostrar un ProgressBar si lo deseas
                            break;
                        case SUCCESS:
                            botonPublicar.setEnabled(true);
                            Toast.makeText(getContext(),
                                            "¡Publicación exitosa!", Toast.LENGTH_SHORT)
                                    .show();
                            // Limpiar campos
                            imagenSeleccionada.setImageResource(R.drawable.plato);
                            editDescripcion.setText("");
                            uriImagen = null;
                            break;
                        case ERROR:
                            botonPublicar.setEnabled(true);
                            Toast.makeText(getContext(),
                                    "Error: " + resource.message,
                                    Toast.LENGTH_LONG).show();
                            break;
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
        ) == PackageManager.PERMISSION_GRANTED) {
            lanzarCamara();
        } else {
            permisoCamaraLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void publicar(NavController navController) {
        String descripcion = editDescripcion.getText()
                .toString().trim();

        if (uriImagen == null || descripcion.isEmpty()) {
            Toast.makeText(getContext(),
                    "Debes seleccionar una imagen y escribir una descripción",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        File imageFile = createTempFileFromUri(uriImagen);
        viewModel.subirPublicacion(imageFile, descripcion);
        navController.navigate(R.id.action_crearPublicacionFragment_to_homeFragment);
    }

    private File createTempFileFromUri(Uri uri) {
        File file = new File(
                requireContext().getCacheDir(),
                "upload_" + System.currentTimeMillis()
        );
        try (InputStream in = requireContext()
                .getContentResolver().openInputStream(uri);
             OutputStream out = new FileOutputStream(file)) {
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
