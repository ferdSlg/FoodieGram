package com.ferd.foodiegram.ui.home;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.ferd.foodiegram.viewmodel.PublicacionViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerPublicaciones;
    private PublicacionAdapter adaptador;
    private PublicacionViewModel pubVM;
    private HomeViewModel homeVM;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1) Referenciar RecyclerView
        recyclerPublicaciones = view.findViewById(R.id.recyclerPublicaciones);
        recyclerPublicaciones.setLayoutManager(new LinearLayoutManager(getContext()));

        // 2) Inicializar ambos ViewModels
        homeVM = new ViewModelProvider(this).get(HomeViewModel.class);
        pubVM  = new ViewModelProvider(requireActivity())
                .get(PublicacionViewModel.class);

        // 3) Crear el adapter con los 3 parámetros correctos
        String idUsuarioActual = FirebaseAuth.getInstance().getCurrentUser().getUid();
        adaptador = new PublicacionAdapter(
                new ArrayList<>(),      // lista vacía al inicio
                pubVM,                  // ViewModel de publicaciones (likes, comentarios, etc.)
                getViewLifecycleOwner(), // LifecycleOwner para LiveData
                idUsuarioActual,
                false
        );
        recyclerPublicaciones.setAdapter(adaptador);

        // 4) Observar la lista de publicaciones desde HomeViewModel
        homeVM.getPublicaciones().observe(
                getViewLifecycleOwner(),
                lista -> adaptador.updateData(lista)
        );
    }
}