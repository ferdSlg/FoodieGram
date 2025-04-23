package com.ferd.foodiegram.data.repositorios;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.ferd.foodiegram.data.supabase.SupabaseClient;
import com.ferd.foodiegram.data.supabase.SupabaseStorageApi;
import com.ferd.foodiegram.model.Publicacion;
import com.ferd.foodiegram.model.Usuario;
import com.ferd.foodiegram.utilidades.Resource;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.io.File;
import java.io.IOException;
import java.net.URLConnection;
import java.util.*;

import okhttp3.*;
import retrofit2.*;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UsuarioRepository {

    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private final SupabaseStorageApi storageApi =
            SupabaseClient.getClient().create(SupabaseStorageApi.class);
    private static final String API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InVvY3JqdmdqcWN6ZGZ5aWR5b29nIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDUwOTg1ODksImV4cCI6MjA2MDY3NDU4OX0.eFt4qe2OKmiekG3azM_sy58H_Ypf1rKQTXMIi8Xs4dU"; // tu anon key
    private static final String PROFILE_BUCKET = "perfiles";

    public LiveData<Usuario> getUsuario(String userId) {
        MutableLiveData<Usuario> live = new MutableLiveData<>();
        firestore.collection("usuarios")
                .document(userId)
                .addSnapshotListener((snap, e) -> {
                    if (e != null) return;
                    Usuario u = snap.toObject(Usuario.class);
                    if (u != null) live.setValue(u);
                });
        return live;
    }

    public LiveData<List<Publicacion>> getPublicacionesUsuario(String userId) {
        MutableLiveData<List<Publicacion>> live = new MutableLiveData<>();
        firestore.collection("publicaciones")
                .whereEqualTo("idUsuario", userId)
                .orderBy("fecha", Query.Direction.DESCENDING)
                .addSnapshotListener((snaps, e) -> {
                    if (e != null) return;
                    List<Publicacion> list = new ArrayList<>();
                    for (var doc : snaps.getDocuments()) {
                        Publicacion p = doc.toObject(Publicacion.class);
                        p.setId(doc.getId());
                        list.add(p);
                    }
                    live.setValue(list);
                });
        return live;
    }

    /**
     * 1) updateEmail (si cambió)
     * 2) updatePassword (si no está vacío)
     * 3) sube foto (si hay) y parchea Firestore con nombre, bio y urlFotoPerfil
     */
    public LiveData<Resource<Void>> updateAuthAndProfile(String userId, String newEmail, String newPassword, String newName, String newBio, File fotoFile) {
        MutableLiveData<Resource<Void>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            result.setValue(Resource.error("Usuario no autenticado"));
            return result;
        }

        // 1) email
        Task<Void> emailTask = user.getEmail() != null && user.getEmail().equals(newEmail)
                ? Tasks.forResult(null)
                : user.updateEmail(newEmail);

        emailTask.addOnCompleteListener(emailRes -> {
            if (!emailRes.isSuccessful()) {
                result.setValue(Resource.error(
                        "Error al actualizar correo: " +
                                emailRes.getException().getMessage()));
                return;
            }

            // 2) password
            Task<Void> pwdTask = (newPassword == null || newPassword.isEmpty())
                    ? Tasks.forResult(null)
                    : user.updatePassword(newPassword);

            pwdTask.addOnCompleteListener(pwdRes -> {
                if (!pwdRes.isSuccessful()) {
                    result.setValue(Resource.error(
                            "Error al actualizar contraseña: " +
                                    pwdRes.getException().getMessage()));
                    return;
                }
                // 3) perfil Firestore + Supabase
                patchFirestoreProfile(userId, newName, newBio, fotoFile, result);
            });
        });

        return result;
    }

    private void patchFirestoreProfile(String userId, String name, String bio, File fotoFile, MutableLiveData<Resource<Void>> live) {
        // Texto-only update
        Runnable patchText = () -> {
            Map<String, Object> data = new HashMap<>();
            data.put("nombre", name);
            data.put("bio", bio);
            firestore.collection("usuarios")
                    .document(userId)
                    .update(data)
                    .addOnSuccessListener(v -> live.setValue(Resource.success(null)))
                    .addOnFailureListener(e -> live.setValue(Resource.error(e.getMessage())));
        };

        if (fotoFile != null) {
            // 1) Asegura que el filename incluya una extensión válida
            String mimeType = URLConnection.guessContentTypeFromName(fotoFile.getName());
            if (mimeType == null) mimeType = "image/jpeg";

            // 2) Prepara el RequestBody con el mime correcto
            RequestBody req = RequestBody.create(
                    MediaType.parse(mimeType),
                    fotoFile
            );
            MultipartBody.Part part = MultipartBody.Part.createFormData(
                    "file",
                    fotoFile.getName(),
                    req
            );

            // 3) Sube la imagen a Supabase incluyendo apikey y Authorization
            //storageApi.uploadImage(API_KEY, "Bearer " + API_KEY, PROFILE_BUCKET, userId + "/" + fotoFile.getName(), true, part)
            // Prepara el Call
            Call<Void> uploadCall = storageApi.uploadImage(
                    API_KEY,
                    "Bearer " + API_KEY,
                    PROFILE_BUCKET,
                    userId + "/" + fotoFile.getName(),
                    true,
                    part
            );

            // Imprime en log la URL completa que se solicitará
            Log.d("SupabaseUpload", "Request URL: " + uploadCall.request().url().toString());

            // Ahora sí, encolamos la llamada
            uploadCall
                    .enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> resp) {
                    /*if (!resp.isSuccessful()) {
                        live.setValue(Resource.error(
                                "Error al subir foto: código " + resp.code()));
                        return;
                    }*/
                    if (!resp.isSuccessful()) {
                        String errorJson = "";
                        try {
                            errorJson = resp.errorBody() != null
                                    ? resp.errorBody().string()
                                    : "sin cuerpo de error";
                        } catch (IOException e) { /*…*/ }
                        Log.e("SupabaseUpload",
                                "UPLOAD ERROR code=" + resp.code()
                                        + " msg=" + resp.message()
                                        + " body=" + errorJson
                        );
                        live.setValue(Resource.error("Error al subir foto: " + errorJson));
                        return;
                    }
                    // 4) Al éxito, toma la URL pública y parchea Firestore
                    String urlFoto = resp.raw().request().url().toString();
                    Map<String, Object> data = new HashMap<>();
                    data.put("nombre", name);
                    data.put("bio", bio);
                    data.put("urlFotoPerfil", urlFoto);
                    firestore.collection("usuarios")
                            .document(userId)
                            .update(data)
                            .addOnSuccessListener(v ->
                                    live.setValue(Resource.success(null)))
                            .addOnFailureListener(e ->
                                    live.setValue(Resource.error(e.getMessage())));
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    live.setValue(Resource.error(
                            "Fallo de red al subir foto: " + t.getMessage()));
                }
            });
        } else {
            // Sin foto, solo parchea texto
            patchText.run();
        }
    }
}