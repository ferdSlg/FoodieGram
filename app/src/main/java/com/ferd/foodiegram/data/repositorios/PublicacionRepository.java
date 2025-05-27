package com.ferd.foodiegram.data.repositorios;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.ferd.foodiegram.data.supabase.SupabaseClient;
import com.ferd.foodiegram.data.supabase.SupabaseStorageApi;
import com.ferd.foodiegram.model.Comentario;
import com.ferd.foodiegram.model.Publicacion;
import com.ferd.foodiegram.model.Usuario;
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

    //Sube imagen y crea publicación en Firestore
    public LiveData<Resource<Void>> subirPublicacion(File imageFile, String descripcion) {
        MutableLiveData<Resource<Void>> resultado = new MutableLiveData<>();
        resultado.setValue(Resource.loading());

        // Preparar multipart
        RequestBody reqFile = RequestBody.create(MediaType.parse("image/*"), imageFile);
        MultipartBody.Part body = MultipartBody.Part.createFormData(
                "file", imageFile.getName(), reqFile);

        storageApi.uploadImage(API_KEY, "Bearer " + API_KEY, "fotos", imageFile.getName(), true, body).enqueue(new Callback<Void>() {
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

    //Obtiene la lista de publicaciones y mostrar solo las publicaciones de los usuarios que estoy siguiendo y las mias
    public LiveData<List<Publicacion>> getFeed(String userId) {
        MutableLiveData<List<Publicacion>> liveData = new MutableLiveData<>();
        firestore.collection("usuarios")
                .document(userId)
                .addSnapshotListener((snap, e) -> {
                    if (e != null || snap == null) return;
                    Usuario me = snap.toObject(Usuario.class);
                    List<String> segs = me.getSeguidos() != null
                            ? new ArrayList<>(me.getSeguidos())
                            : new ArrayList<>();
                    segs.add(userId); // incluir mis propias publicaciones

                    //escuchamos todas las publicaciones
                    firestore.collection("publicaciones")
                            .orderBy("fecha", Query.Direction.DESCENDING)
                            .addSnapshotListener((snaps2, e2) -> {
                                if (e2 != null) return;
                                List<Publicacion> feed = new ArrayList<>();
                                for (var doc : snaps2.getDocuments()) {
                                    Publicacion p = doc.toObject(Publicacion.class);
                                    if (segs.contains(p.getIdUsuario())) {
                                        p.setId(doc.getId());
                                        feed.add(p);
                                    }
                                }
                                liveData.setValue(feed);
                            });
                });
        return liveData;
    }

    // Likes
    public LiveData<Resource<Void>> likePost(String postId) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        MutableLiveData<Resource<Void>> result = new MutableLiveData<>();
        firestore.collection("publicaciones")
                .document(postId)
                .collection("likes")
                .document(uid)
                .set(new HashMap<>())
                .addOnSuccessListener(v -> result.setValue(Resource.success(null)))
                .addOnFailureListener(e -> result.setValue(Resource.error(e.getMessage())));
        return result;
    }

    public LiveData<Resource<Void>> unlikePost(String postId) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        MutableLiveData<Resource<Void>> result = new MutableLiveData<>();
        firestore.collection("publicaciones")
                .document(postId)
                .collection("likes")
                .document(uid)
                .delete()
                .addOnSuccessListener(v -> result.setValue(Resource.success(null)))
                .addOnFailureListener(e -> result.setValue(Resource.error(e.getMessage())));
        return result;
    }

    public LiveData<Boolean> isPostLiked(String postId) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        MutableLiveData<Boolean> result = new MutableLiveData<>(false);
        firestore.collection("publicaciones")
                .document(postId)
                .collection("likes")
                .document(uid)
                .addSnapshotListener((snap, e) -> {
                    result.setValue(snap != null && snap.exists());
                });
        return result;
    }

    public LiveData<Integer> getLikesCount(String postId) {
        MutableLiveData<Integer> result = new MutableLiveData<>(0);
        firestore.collection("publicaciones")
                .document(postId)
                .collection("likes")
                .addSnapshotListener((snap, e) -> {
                    if (snap != null) result.setValue(snap.size());
                });
        return result;
    }

    // Comentarios
    public LiveData<Resource<Void>> addComment(String postId, Comentario comentario) {
        MutableLiveData<Resource<Void>> result = new MutableLiveData<>();
        firestore.collection("publicaciones")
                .document(postId)
                .collection("comentarios")
                .add(comentario)
                .addOnSuccessListener(doc -> result.setValue(Resource.success(null)))
                .addOnFailureListener(e -> result.setValue(Resource.error(e.getMessage())));
        return result;
    }

    public LiveData<List<Comentario>> getComments(String postId) {
        MutableLiveData<List<Comentario>> result = new MutableLiveData<>(new ArrayList<>());
        firestore.collection("publicaciones")
                .document(postId)
                .collection("comentarios")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((snap, e) -> {
                    if (snap != null) {
                        result.setValue(snap.toObjects(Comentario.class));
                    }
                });
        return result;
    }

    // Editar publicación
    public LiveData<Resource<Void>> updatePost(String postId, String nuevaDesc) {
        MutableLiveData<Resource<Void>> result = new MutableLiveData<>();
        firestore.collection("publicaciones")
                .document(postId)
                .update("descripcion", nuevaDesc)
                .addOnSuccessListener(v -> result.setValue(Resource.success(null)))
                .addOnFailureListener(e -> result.setValue(Resource.error(e.getMessage())));
        return result;
    }

    //Eliminar publicación y su imagen
    public LiveData<Resource<Void>> deletePost(String postId, String imagePath) {
        MutableLiveData<Resource<Void>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        //Eliminar comentarios
        firestore.collection("publicaciones")
                .document(postId)
                .collection("comentarios")
                .get()
                .addOnSuccessListener(commentsSnap -> {
                    for (var doc : commentsSnap.getDocuments()) {
                        doc.getReference().delete();
                    }

                    // Eliminar likes
                    firestore.collection("publicaciones")
                            .document(postId)
                            .collection("likes")
                            .get()
                            .addOnSuccessListener(likesSnap -> {
                                for (var doc : likesSnap.getDocuments()) {
                                    doc.getReference().delete();
                                }

                                //Eliminar la publicación
                                firestore.collection("publicaciones")
                                        .document(postId)
                                        .delete()
                                        .addOnSuccessListener(v -> {
                                            //Eliminar imagen de Supabase
                                            storageApi.deleteImage(API_KEY, "Bearer " + API_KEY, "fotos", imagePath)
                                                    .enqueue(new Callback<Void>() {
                                                        @Override
                                                        public void onResponse(Call<Void> call, Response<Void> response) {
                                                            result.setValue(Resource.success(null));
                                                        }

                                                        @Override
                                                        public void onFailure(Call<Void> call, Throwable t) {
                                                            result.setValue(Resource.error("Error al eliminar imagen: " + t.getMessage()));
                                                        }
                                                    });
                                        })
                                        .addOnFailureListener(e ->
                                                result.setValue(Resource.error("Error al eliminar publicación: " + e.getMessage())));
                            })
                            .addOnFailureListener(e ->
                                    result.setValue(Resource.error("Error al eliminar likes: " + e.getMessage())));
                })
                .addOnFailureListener(e ->
                        result.setValue(Resource.error("Error al eliminar comentarios: " + e.getMessage())));

        return result;
    }
}

