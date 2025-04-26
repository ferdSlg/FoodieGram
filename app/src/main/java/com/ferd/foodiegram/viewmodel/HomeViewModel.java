package com.ferd.foodiegram.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.ferd.foodiegram.data.repositorios.PublicacionRepository;
import com.ferd.foodiegram.model.Publicacion;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class HomeViewModel extends ViewModel {
    private final PublicacionRepository repo = new PublicacionRepository();
    private final LiveData<List<Publicacion>> publicaciones;

    public HomeViewModel() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        publicaciones = repo.getFeed(uid);
    }

    public LiveData<List<Publicacion>> getPublicaciones() {
        return publicaciones;
    }
}
