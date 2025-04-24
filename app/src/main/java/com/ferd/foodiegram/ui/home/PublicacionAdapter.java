package com.ferd.foodiegram.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.ferd.foodiegram.R;
import com.ferd.foodiegram.model.Publicacion;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class PublicacionAdapter extends RecyclerView.Adapter<PublicacionAdapter.PublicacionViewHolder> {

    private final List<Publicacion> listaPublicaciones;

    public PublicacionAdapter(List<Publicacion> listaPublicaciones) {
        this.listaPublicaciones = listaPublicaciones;
    }

    @NonNull
    @Override
    public PublicacionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_publicacion, parent, false);
        return new PublicacionViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull PublicacionViewHolder holder, int position) {
        Publicacion publicacion = listaPublicaciones.get(position);

        Glide.with(holder.itemView.getContext()).load(publicacion.getUrlFotoComida()).placeholder(R.drawable.plato).into(holder.imagenComida);

        FirebaseFirestore.getInstance()
                .collection("usuarios")
                .document(publicacion.getIdUsuario())
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String nombre   = doc.getString("nombre");
                        String fotoPerf = doc.getString("urlFotoPerfil");
                        holder.textNombreUsuario.setText(nombre);
                        Glide.with(holder.itemView.getContext()).load(fotoPerf).circleCrop().into(holder.imagenPerfil);
                    }
                })
                .addOnFailureListener(e -> {
                    // Fallback al email si algo falla
                    holder.textNombreUsuario.setText(publicacion.getNombreUsuario());
                });
        holder.textDescripcion.setText(publicacion.getDescripcion());
    }

    @Override
    public int getItemCount() {
        return listaPublicaciones.size();
    }

    public void updateData(List<Publicacion> nuevas) {
        listaPublicaciones.clear();
        listaPublicaciones.addAll(nuevas);
        notifyDataSetChanged();
    }

    static class PublicacionViewHolder extends RecyclerView.ViewHolder {
        TextView textNombreUsuario, textDescripcion;
        ImageView imagenPerfil, imagenComida;

        public PublicacionViewHolder(@NonNull View itemView) {
            super(itemView);
            imagenPerfil      = itemView.findViewById(R.id.avatarUsuario);
            textNombreUsuario = itemView.findViewById(R.id.textNombreUsuario);
            textDescripcion = itemView.findViewById(R.id.textDescripcion);
            imagenComida = itemView.findViewById(R.id.imagenComida);
        }
    }
}
