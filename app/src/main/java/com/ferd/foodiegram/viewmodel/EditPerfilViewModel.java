package com.ferd.foodiegram.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import com.ferd.foodiegram.data.repositorios.UsuarioRepository;
import com.ferd.foodiegram.utilidades.Resource;
import com.google.firebase.auth.FirebaseAuth;
import java.io.File;

public class EditPerfilViewModel extends AndroidViewModel {
    private final UsuarioRepository repo = new UsuarioRepository();
    private final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
    private final MediatorLiveData<Resource<Void>> resultado = new MediatorLiveData<>();

    public EditPerfilViewModel(@NonNull Application app) {
        super(app);
    }

    public LiveData<Resource<Void>> getResultado() {
        return resultado;
    }

    public void actualizar(
            String newEmail,
            String newPassword,
            String newName,
            String newBio,
            File fotoFile
    ) {
        LiveData<Resource<Void>> call = repo.updateAuthAndProfile(
                uid, newEmail, newPassword, newName, newBio, fotoFile
        );
        resultado.addSource(call, resultado::setValue);
    }
}
