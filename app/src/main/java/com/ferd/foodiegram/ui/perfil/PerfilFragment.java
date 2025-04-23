package com.ferd.foodiegram.ui.perfil;

import android.os.Bundle;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.*;

import com.bumptech.glide.Glide;
import com.ferd.foodiegram.R;
import com.ferd.foodiegram.model.Publicacion;
import com.ferd.foodiegram.model.Usuario;
import com.ferd.foodiegram.ui.home.PublicacionAdapter;
import com.ferd.foodiegram.viewmodel.PerfilViewModel;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;


public class PerfilFragment extends Fragment {
    private ImageView imgPerfil;
    private TextView txtNombre, txtBio, txtSeguidores, txtSeguidos;
    private Button btnEditar;
    private RecyclerView rvPosts;
    private PublicacionAdapter adapter;
    private PerfilViewModel vm;

    @Override
    public View onCreateView(@NonNull LayoutInflater inf, ViewGroup c, Bundle b) {
        View v = inf.inflate(R.layout.fragment_perfil, c, false);

        imgPerfil = v.findViewById(R.id.imgPerfil);
        txtNombre = v.findViewById(R.id.txtNombre);
        txtBio = v.findViewById(R.id.txtBio);
        txtSeguidores = v.findViewById(R.id.txtSeguidores);
        txtSeguidos = v.findViewById(R.id.txtSeguidos);
        btnEditar = v.findViewById(R.id.btnEditarPerfil);
        rvPosts = v.findViewById(R.id.recyclerMisPosts);

        rvPosts.setLayoutManager(new GridLayoutManager(getContext(), 1));
        adapter = new PublicacionAdapter(new ArrayList<>());
        rvPosts.setAdapter(adapter);

        vm = new ViewModelProvider(this).get(PerfilViewModel.class);

        vm.getUsuario().observe(getViewLifecycleOwner(), this::bindUsuario);
        vm.getMisPublicaciones().observe(getViewLifecycleOwner(), lista ->
                adapter.updateData(lista)
        );

        btnEditar.setOnClickListener(v2 ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_perfilFragment_to_editPerfilFragment)
        );

        return v;
    }

    private void bindUsuario(Usuario u) {
        txtNombre.setText(u.getNombre());
        txtBio.setText(u.getBio());
        txtSeguidores.setText(String.valueOf(u.getSeguidores().size()));
        txtSeguidos.setText(String.valueOf(u.getSeguidos().size()));
        Glide.with(this)
                .load(u.getUrlFotoPerfil())
                .placeholder(R.drawable.user)
                .into(imgPerfil);
    }
}