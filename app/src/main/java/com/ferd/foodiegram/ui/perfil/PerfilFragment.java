package com.ferd.foodiegram.ui.perfil;

import android.content.Intent;
import android.os.Bundle;
import android.view.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.*;

import com.bumptech.glide.Glide;
import com.ferd.foodiegram.AuthActivity;
import com.ferd.foodiegram.R;
import com.ferd.foodiegram.databinding.FragmentPerfilBinding;
import com.ferd.foodiegram.model.Usuario;
import com.ferd.foodiegram.ui.home.PublicacionAdapter;
import com.ferd.foodiegram.viewmodel.PerfilViewModel;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class PerfilFragment extends Fragment {
    private FragmentPerfilBinding binding;
    private PublicacionAdapter adapter;
    private PerfilViewModel vm;

    @Override
    public View onCreateView(@NonNull LayoutInflater inf, ViewGroup c, Bundle b) {
        binding = FragmentPerfilBinding.inflate(inf, c, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.recyclerMisPosts.setLayoutManager(new GridLayoutManager(getContext(), 1));
        adapter = new PublicacionAdapter(new ArrayList<>());
        binding.recyclerMisPosts.setAdapter(adapter);

        vm = new ViewModelProvider(this).get(PerfilViewModel.class);
        vm.getUsuario().observe(getViewLifecycleOwner(), this::bindUsuario);
        vm.getMisPublicaciones().observe(getViewLifecycleOwner(), lista -> adapter.updateData(lista));
        binding.btnEditarPerfil.setOnClickListener(v2 ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_perfilFragment_to_editPerfilFragment)
        );
        binding.botonCerrarSesion.setOnClickListener(v3 -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(requireContext(), AuthActivity.class);
            startActivity(intent);
            requireActivity().finish();
        });
    }

    //Método para actualizar los datos del usuario
    private void bindUsuario(Usuario u) {
        binding.txtNombre.setText(u.getNombre());
        binding.txtBio.setText(u.getBio());
        binding.txtSeguidores.setText(String.valueOf(u.getSeguidores().size()));
        binding.txtSeguidos.setText(String.valueOf(u.getSeguidos().size()));
        Glide.with(this)
                .load(u.getUrlFotoPerfil())
                .placeholder(R.drawable.user)
                .into(binding.imgPerfil);
    }
}