package com.ferd.foodiegram.ui.home;

import android.util.Log;
import android.view.*;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.ferd.foodiegram.R;
import com.ferd.foodiegram.model.Usuario;
import com.ferd.foodiegram.viewmodel.BuscarAmigoViewModel;

import java.util.List;

public class AmigoAdapter extends RecyclerView.Adapter<AmigoAdapter.UsuarioVH> {
    private final List<Usuario> usuario;
    private final BuscarAmigoViewModel vm;
    private final LifecycleOwner owner;

    public AmigoAdapter(List<Usuario> usuario, BuscarAmigoViewModel vm, LifecycleOwner owner) {
        this.usuario = usuario;
        this.vm    = vm;
        this.owner = owner;
    }

    public void updateData(List<Usuario> nueva) {
        usuario.clear();
        usuario.addAll(nueva);
        notifyDataSetChanged();
    }

    @Override
    public UsuarioVH onCreateViewHolder(ViewGroup p, int v) {
        View vew = LayoutInflater.from(p.getContext())
                .inflate(R.layout.item_usuario, p, false);
        return new UsuarioVH(vew);
    }

    @Override
    public void onBindViewHolder(UsuarioVH h, int pos) {
        Usuario u = usuario.get(pos);
        Log.d("FriendAdapter", "Bind user at pos " + pos + ": " + u.getNombre());//para prueba
        h.tvName.setText(u.getNombre());
        h.tvEmail.setText(u.getCorreo());
        Glide.with(h.itemView).load(u.getUrlFotoPerfil())
                .circleCrop()
                .into(h.img);

        // Actualizar estado del botón
        vm.isFollowing(u.getId()).observe(owner, isF -> {
            h.btnFollow.setText(isF ? "Dejar de seguir" : "Seguir");
        });

        h.btnFollow.setOnClickListener(v -> {
            if (h.btnFollow.getText().equals("Seguir")) {
                vm.follow(u.getId());
            } else {
                vm.unfollow(u.getId());
            }
        });
    }

    @Override public int getItemCount() { return usuario.size(); }

    static class UsuarioVH extends RecyclerView.ViewHolder {
        ImageView img;
        TextView tvName, tvEmail;
        Button btnFollow;
        UsuarioVH(View iv) {
            super(iv);
            img        = iv.findViewById(R.id.imgPerfil);
            tvName     = iv.findViewById(R.id.tvNombreUsuario);
            tvEmail    = iv.findViewById(R.id.tvCorreoUsuario);
            btnFollow  = iv.findViewById(R.id.btnFollow);
        }
    }
}
