package com.ferd.foodiegram.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.ferd.foodiegram.data.repositorios.FollowRepository;
import com.ferd.foodiegram.data.repositorios.UsuarioRepository;
import com.ferd.foodiegram.model.Usuario;

import java.util.List;

public class BuscarAmigoViewModel extends ViewModel{
    private final UsuarioRepository usuarioRepo = new UsuarioRepository();
    private final FollowRepository followRepo = new FollowRepository();

    private final MutableLiveData<String> query = new MutableLiveData<>();
    public final LiveData<List<Usuario>> results =
            Transformations.switchMap(query, q -> usuarioRepo.buscarUsuarios(q));

    public void setQuery(String q) {
        query.setValue(q);
    }

    public LiveData<Void> follow(String uid) {
        return followRepo.followUser(uid);
    }

    public LiveData<Void> unfollow(String uid) {
        return followRepo.unfollowUser(uid);
    }

    public LiveData<Boolean> isFollowing(String uid) {
        return followRepo.isFollowing(uid);
    }
}
