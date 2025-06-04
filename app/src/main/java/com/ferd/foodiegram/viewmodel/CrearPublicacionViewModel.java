package com.ferd.foodiegram.viewmodel;

import com.ferd.foodiegram.utilidades.Resource;

import android.app.Application;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.ferd.foodiegram.data.repositorios.PublicacionRepository;

import java.io.File;

public class    CrearPublicacionViewModel extends AndroidViewModel {

    private final PublicacionRepository repo;
    private final MutableLiveData<Uri> imagenSeleccionada = new MutableLiveData<>();
    private final LiveData<Resource<Void>> resultadoSubida;

    public CrearPublicacionViewModel(@NonNull Application app) {
        super(app);
        repo = new PublicacionRepository();
        // Inicialmente null, se disparará al llamar a subir(...)
        resultadoSubida = new MediatorLiveData<>();
    }

    public LiveData<Uri> getImagenSeleccionada() {
        return imagenSeleccionada;
    }

    public LiveData<Resource<Void>> getResultadoSubida() {
        return resultadoSubida;
    }

    public void setImagen(Uri uri) {
        imagenSeleccionada.setValue(uri);
    }

    public void subirPublicacion(File imageFile, String descripcion) {
        // Dispara la llamada al repo
        LiveData<Resource<Void>> call = repo.subirPublicacion(imageFile, descripcion);
        ((MediatorLiveData<Resource<Void>>) resultadoSubida).addSource(call, resource -> {
            ((MediatorLiveData<Resource<Void>>) resultadoSubida).setValue(resource);
        });
    }
}
