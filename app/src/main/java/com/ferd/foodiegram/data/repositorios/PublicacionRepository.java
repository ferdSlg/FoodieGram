package com.ferd.foodiegram.data.repositorios;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.ferd.foodiegram.data.supabase.SupabaseClient;
import com.ferd.foodiegram.data.supabase.SupabaseStorageApi;
import com.ferd.foodiegram.model.Publicacion;
import com.ferd.foodiegram.utilidades.Resource;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PublicacionRepository {

    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private final SupabaseStorageApi storageApi;
    private static final String API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InVvY3JqdmdqcWN6ZGZ5aWR5b29nIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDUwOTg1ODksImV4cCI6MjA2MDY3NDU4OX0.eFt4qe2OKmiekG3azM_sy58H_Ypf1rKQTXMIi8Xs4dU";

    public PublicacionRepository() {
        storageApi = SupabaseClient
                .getClient()
                .create(SupabaseStorageApi.class);
    }

    // 1) Sube imagen y crea publicación en Firestore
    public LiveData<Resource<Void>> subirPublicacion(File imageFile, String descripcion) {
        MutableLiveData<Resource<Void>> resultado = new MutableLiveData<>();
        resultado.setValue(Resource.loading());

        // Preparar multipart
        RequestBody reqFile = RequestBody.create(MediaType.parse("image/*"), imageFile);
        MultipartBody.Part body = MultipartBody.Part.createFormData(
                "file", imageFile.getName(), reqFile);

        storageApi.uploadImage(API_KEY, "Bearer " + API_KEY, "fotos", imageFile.getName(), body)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (!response.isSuccessful()) {
                            resultado.setValue(Resource.error("Error al subir imagen"));
                            return;
                        }
                        String fileUrl = response.raw().request().url().toString();
                        // Guardar en Firestore
                        Map<String, Object> datos = new HashMap<>();
                        datos.put("urlFotoComida", fileUrl);
                        datos.put("descripcion", descripcion);
                        datos.put("fecha", System.currentTimeMillis());
                        datos.put("idUsuario", FirebaseAuth.getInstance().getCurrentUser().getUid());
                        datos.put("nombreUsuario", FirebaseAuth.getInstance().getCurrentUser().getEmail());

                        firestore.collection("publicaciones")
                                .add(datos)
                                .addOnSuccessListener(docRef ->
                                        resultado.setValue(Resource.success(null)))
                                .addOnFailureListener(e ->
                                        resultado.setValue(Resource.error(e.getMessage())));
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        resultado.setValue(Resource.error(t.getMessage()));
                    }
                });

        return resultado;
    }

    // 2) Obtiene la lista de publicaciones
    public LiveData<List<Publicacion>> getPublicaciones() {
        MutableLiveData<List<Publicacion>> liveData = new MutableLiveData<>();
        firestore.collection("publicaciones")
                .orderBy("fecha", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) return;
                    List<Publicacion> lista = new ArrayList<>();
                    for (var doc : snapshots.getDocuments()) {
                        Publicacion pub = doc.toObject(Publicacion.class);
                        pub.setId(doc.getId());
                        lista.add(pub);
                    }
                    liveData.setValue(lista);
                });
        return liveData;
    }
}

