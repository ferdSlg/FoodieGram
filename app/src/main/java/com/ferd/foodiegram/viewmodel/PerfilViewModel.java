package com.ferd.foodiegram.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.ferd.foodiegram.data.repositorios.PublicacionRepository;
import com.ferd.foodiegram.data.repositorios.UsuarioRepository;
import com.ferd.foodiegram.model.Publicacion;
import com.ferd.foodiegram.model.Usuario;
import com.ferd.foodiegram.utilidades.Resource;
import com.google.firebase.auth.FirebaseAuth;
import java.util.List;

public class PerfilViewModel extends ViewModel {
    private final UsuarioRepository repo = new UsuarioRepository();
    private final PublicacionRepository repoPublicacion = new PublicacionRepository();
    private final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

    public LiveData<Usuario> getUsuario() {
        return repo.getUsuario(uid);
    }

    public LiveData<List<Publicacion>> getMisPublicaciones() {
        return repo.getPublicacionesUsuario(uid);
    }

    public LiveData<Resource<Void>> eliminarPublicacion(String postId, String imagePath) {
        return repoPublicacion.deletePost(postId, imagePath);
    }
}
