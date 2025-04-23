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
        holder.textNombreUsuario.setText(publicacion.getNombreUsuario());
        holder.textDescripcion.setText(publicacion.getDescripcion());

        Glide.with(holder.itemView.getContext())
                .load(publicacion.getUrlFotoComida())
                .placeholder(R.drawable.plato)
                .into(holder.imagenComida);
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
        ImageView imagenComida;

        public PublicacionViewHolder(@NonNull View itemView) {
            super(itemView);
            textNombreUsuario = itemView.findViewById(R.id.textNombreUsuario);
            textDescripcion = itemView.findViewById(R.id.textDescripcion);
            imagenComida = itemView.findViewById(R.id.imagenComida);
        }
    }
}
