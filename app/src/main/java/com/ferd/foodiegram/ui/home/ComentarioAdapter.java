package com.ferd.foodiegram.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.ferd.foodiegram.R;
import com.ferd.foodiegram.model.Comentario;
import com.ferd.foodiegram.model.Usuario;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DateFormat;
import java.util.List;

public class ComentarioAdapter extends RecyclerView.Adapter<ComentarioAdapter.ComentarioViewHolder> {
    private final List<Comentario> comentarios;

    public ComentarioAdapter(List<Comentario> comentarios) {
        this.comentarios = comentarios;
    }

    @NonNull
    @Override
    public ComentarioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_comentario, parent, false);
        return new ComentarioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ComentarioViewHolder holder, int position) {
        Comentario c = comentarios.get(position);
        //1 Consultar autor
        FirebaseFirestore.getInstance()
                .collection("usuarios")
                .document(c.getUidAutor())
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Usuario u = doc.toObject(Usuario.class);
                        holder.tvNombre.setText(u.getNombre());
                        Glide.with(holder.itemView.getContext())
                                .load(u.getUrlFotoPerfil())
                                .circleCrop()
                                .into(holder.imgAvatar);
                    }
                });
        //2 Texto y timestamp
        holder.tvTexto.setText(c.getTexto());
        String fecha = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
                .format(c.getTimestamp());
        holder.tvTimestamp.setText(fecha);
    }

    @Override
    public int getItemCount() {
        return comentarios.size();
    }

    public void updateData(List<Comentario> nuevas) {
        comentarios.clear();
        comentarios.addAll(nuevas);
        notifyDataSetChanged();
    }

    static class ComentarioViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView imgAvatar;
        TextView tvNombre, tvTexto, tvTimestamp;

        ComentarioViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar   = itemView.findViewById(R.id.imgAvatarComentario);
            tvNombre    = itemView.findViewById(R.id.tvNombreComentario);
            tvTexto     = itemView.findViewById(R.id.tvTextoComentario);
            tvTimestamp = itemView.findViewById(R.id.tvTimestampComentario);
        }
    }
}