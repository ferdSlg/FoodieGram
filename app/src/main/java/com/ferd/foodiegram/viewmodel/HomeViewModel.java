package com.ferd.foodiegram.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.ferd.foodiegram.data.repositorios.PublicacionRepository;
import com.ferd.foodiegram.model.Publicacion;

import java.util.List;

public class HomeViewModel extends ViewModel {
    private final PublicacionRepository repo = new PublicacionRepository();
    private final LiveData<List<Publicacion>> publicaciones = repo.getPublicaciones();

    public LiveData<List<Publicacion>> getPublicaciones() {
        return publicaciones;
    }
}
