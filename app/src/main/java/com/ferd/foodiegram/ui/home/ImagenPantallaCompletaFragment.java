package com.ferd.foodiegram.ui.home;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.ferd.foodiegram.R;


public class ImagenPantallaCompletaFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflador, ViewGroup contenedor, Bundle savedInstanceState) {
        View vista = inflador.inflate(R.layout.fragment_imagen_pantalla_completa, contenedor, false);
        ImageView imagenCompleta = vista.findViewById(R.id.imagen_completa);

        // Obtener URL desde argumentos
        String urlImagen = getArguments().getString("urlImagen");
        Glide.with(this).load(urlImagen).into(imagenCompleta);

        // Clic para volver atrás
        imagenCompleta.setOnClickListener(v -> requireActivity().onBackPressed());

        return vista;
    }
}