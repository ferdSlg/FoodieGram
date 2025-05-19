package com.ferd.foodiegram.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.ferd.foodiegram.data.repositorios.PublicacionRepository;
import com.ferd.foodiegram.model.Comentario;
import com.ferd.foodiegram.utilidades.Resource;

import java.util.List;

public class PublicacionViewModel extends ViewModel {
    private final PublicacionRepository repo;

    public PublicacionViewModel() {
        repo = new PublicacionRepository();
    }

    // Likes
    public LiveData<Boolean> isLiked(String postId) {
        return repo.isPostLiked(postId);
    }

    public LiveData<Integer> getLikeCount(String postId) {
        return repo.getLikesCount(postId);
    }

    public LiveData<Resource<Void>> like(String postId) {
        return repo.likePost(postId);
    }

    public LiveData<Resource<Void>> unlike(String postId) {
        return repo.unlikePost(postId);
    }

    // Comentarios
    public LiveData<List<Comentario>> getComments(String postId) {
        return repo.getComments(postId);
    }

    public LiveData<Resource<Void>> addComment(String postId, String texto) {
        Comentario comentario = new Comentario(
                com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid(),
                texto,
                System.currentTimeMillis()
        );
        return repo.addComment(postId, comentario);
    }

    // Edición
    public LiveData<Resource<Void>> updatePost(String postId, String nuevaDesc) {
        return repo.updatePost(postId, nuevaDesc);
    }

    // Eliminación
    public LiveData<Resource<Void>> deletePost(String postId, String imagePath) {
        return repo.deletePost(postId, imagePath);
    }
}
