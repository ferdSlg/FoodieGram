package com.ferd.foodiegram.ui.home;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ferd.foodiegram.R;
import com.ferd.foodiegram.model.Publicacion;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerPublicaciones;
    private PublicacionAdapter adaptador;
    private List<Publicacion> listaPublicaciones;
    private FirebaseFirestore firestore;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View vista = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerPublicaciones = vista.findViewById(R.id.recyclerPublicaciones);
        recyclerPublicaciones.setLayoutManager(new LinearLayoutManager(getContext()));
        listaPublicaciones = new ArrayList<>();
        adaptador = new PublicacionAdapter(listaPublicaciones);
        recyclerPublicaciones.setAdapter(adaptador);

        firestore = FirebaseFirestore.getInstance();
        cargarPublicaciones();

        return vista;
    }

    private void cargarPublicaciones() {
        firestore.collection("publicaciones")
                .orderBy("fecha", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    listaPublicaciones.clear();
                    for (var doc : queryDocumentSnapshots) {
                        Publicacion pub = doc.toObject(Publicacion.class);
                        pub.setId(doc.getId());
                        listaPublicaciones.add(pub);
                    }
                    adaptador.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                });
    }
}