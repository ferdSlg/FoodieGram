package com.ferd.foodiegram.ui.home;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.ferd.foodiegram.R;
import com.ferd.foodiegram.model.Publicacion;
import com.ferd.foodiegram.model.Usuario;
import com.ferd.foodiegram.utilidades.Resource;
import com.ferd.foodiegram.viewmodel.PublicacionViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class PublicacionAdapter extends RecyclerView.Adapter<PublicacionAdapter.PublicacionViewHolder> {
    private final List<Publicacion> listaPublicaciones;
    private final PublicacionViewModel viewModel;
    private final LifecycleOwner lifecycleOwner;
    private final String idUsuarioActual;
    private final boolean esPerfil;

    public PublicacionAdapter(List<Publicacion> listaPublicaciones, PublicacionViewModel viewModel, LifecycleOwner lifecycleOwner, String idUsuarioActual, boolean esPerfil) {
        this.listaPublicaciones = listaPublicaciones;
        this.viewModel = viewModel;
        this.lifecycleOwner = lifecycleOwner;
        this.idUsuarioActual = idUsuarioActual;
        this.esPerfil = esPerfil;
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
        String postId = publicacion.getId();

        // Cargar avatar y nombre de usuario desde Firestore
        FirebaseFirestore.getInstance()
                .collection("usuarios")
                .document(publicacion.getIdUsuario())
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Usuario usuario = doc.toObject(Usuario.class);
                        if (usuario != null) {
                            holder.textNombreUsuario.setText(usuario.getNombre());
                            Glide.with(holder.itemView.getContext())
                                    .load(usuario.getUrlFotoPerfil())
                                    .circleCrop()
                                    .placeholder(R.drawable.user)
                                    .into(holder.imagenPerfil);

                            holder.imagenPerfil.setOnClickListener(v -> {
                                NavController navController = Navigation.findNavController(v);
                                Bundle args = new Bundle();
                                args.putString("urlImagen", usuario.getUrlFotoPerfil());
                                navController.navigate(R.id.imagenPantallaCompletaFragment, args);
                            });
                        }
                    } else {
                        holder.textNombreUsuario.setText(publicacion.getNombreUsuario());
                    }
                })
                .addOnFailureListener(e -> {
                    holder.textNombreUsuario.setText(publicacion.getNombreUsuario());
                });

        // Cargar imagen de la publicación
        Glide.with(holder.itemView.getContext())
                .load(publicacion.getUrlFotoComida())
                .placeholder(R.drawable.plato)
                .into(holder.imagenComida);

        // Descripción
        holder.textDescripcion.setText(publicacion.getDescripcion());

        // Likes
        viewModel.isLiked(postId).observe(lifecycleOwner, liked -> {
            holder.isLiked = liked;  // ← guardamos el estado
            holder.btnLike.setIconResource(
                    liked ? R.drawable.corazon   // estado liked
                            : R.drawable.favorito // estado unliked
            );
        });
        viewModel.getLikeCount(postId).observe(lifecycleOwner, count ->
                holder.btnLike.setText(String.valueOf(count))
        );

        holder.btnLike.setOnClickListener(v -> {
            if (holder.isLiked) {
                viewModel.unlike(postId);
            } else {
                viewModel.like(postId);
            }
        });

        // Comentarios
        viewModel.getComments(postId).observe(lifecycleOwner, lista ->
                holder.btnComment.setText(String.valueOf(lista.size()))
        );
        holder.btnComment.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("postId", postId);
            Navigation.findNavController(v)
                    .navigate(R.id.comentariosFragment, bundle);//cambio a comentarioFragment para que navegue desde cualquier sitio que se llame
        });

        //eliminar publicación si es tu publicación
        if (esPerfil && publicacion.getIdUsuario().equals(idUsuarioActual)) {
            holder.btnEliminar.setVisibility(View.VISIBLE);
            holder.btnEliminar.setOnClickListener(v -> {
                String imagePath = obtenerRutaDeImagen(publicacion.getUrlFotoComida());

                // Confirmar antes de eliminar (opcional)
                new AlertDialog.Builder(holder.itemView.getContext())
                        .setTitle("Eliminar publicación")
                        .setMessage("¿Estás seguro de que deseas eliminar esta publicación?")
                        .setPositiveButton("Sí", (dialog, which) -> {
                            viewModel.eliminarPost(publicacion.getId(), imagePath)
                                    .observe(lifecycleOwner, res -> {
                                        if (res.status == Resource.Status.SUCCESS) {
                                            listaPublicaciones.remove(holder.getAdapterPosition());
                                            notifyItemRemoved(holder.getAdapterPosition());
                                        } else if (res.status == Resource.Status.ERROR) {
                                            Toast.makeText(holder.itemView.getContext(),
                                                    "Error: " + res.message, Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        })
                        .setNegativeButton("Cancelar", null)
                        .show();
            });
        } else {
            holder.btnEliminar.setVisibility(View.GONE);
        }

        // Ver imagen a pantalla completa
        holder.imagenComida.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            Bundle args = new Bundle();
            args.putString("urlImagen", publicacion.getUrlFotoComida());
            navController.navigate(R.id.imagenPantallaCompletaFragment, args);
        });
    }

    @Override
    public int getItemCount() {
        return listaPublicaciones.size();
    }

    //Actualiza la lista de publicaciones.
    public void updateData(List<Publicacion> nuevas) {
        listaPublicaciones.clear();
        listaPublicaciones.addAll(nuevas);
        notifyDataSetChanged();
    }

    static class PublicacionViewHolder extends RecyclerView.ViewHolder {
        ImageView imagenPerfil, imagenComida;
        TextView textNombreUsuario, textDescripcion;
        MaterialButton btnLike, btnComment, btnEliminar;
        boolean isLiked = false;
        PublicacionViewHolder(@NonNull View itemView) {
            super(itemView);
            imagenPerfil = itemView.findViewById(R.id.avatarUsuario);
            textNombreUsuario = itemView.findViewById(R.id.textNombreUsuario);
            imagenComida = itemView.findViewById(R.id.imagenComida);
            textDescripcion = itemView.findViewById(R.id.textDescripcion);
            btnLike = itemView.findViewById(R.id.btnLike);
            btnComment = itemView.findViewById(R.id.btnComentarios);
            btnEliminar = itemView.findViewById(R.id.btnEliminar);
        }
    }

    private String obtenerRutaDeImagen(String urlCompleta) {
        int index = urlCompleta.lastIndexOf('/');
        return index != -1 ? urlCompleta.substring(index + 1) : urlCompleta;
    }
}

