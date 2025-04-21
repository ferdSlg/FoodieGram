package com.ferd.foodiegram.ui.home;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ferd.foodiegram.R;
import com.ferd.foodiegram.model.Publicacion;
import com.ferd.foodiegram.viewmodel.HomeViewModel;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerPublicaciones;
    private PublicacionAdapter adaptador;
    private HomeViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View vista = inflater.inflate(
                R.layout.fragment_home,
                container,
                false
        );

        // Configurar RecyclerView
        recyclerPublicaciones = vista.findViewById(R.id.recyclerPublicaciones);
        recyclerPublicaciones.setLayoutManager(new LinearLayoutManager(getContext()));
        adaptador = new PublicacionAdapter(new ArrayList<>());
        recyclerPublicaciones.setAdapter(adaptador);

        // Inicializar ViewModel
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        // Observar cambios en las publicaciones
        viewModel.getPublicaciones().observe(
                getViewLifecycleOwner(), lista -> {
                    // Actualizar datos del adaptador
                    adaptador.updateData(lista);
                }
        );

        return vista;
    }
}