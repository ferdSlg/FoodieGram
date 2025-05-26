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
import com.ferd.foodiegram.viewmodel.PublicacionViewModel;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class PerfilFragment extends Fragment {
    private FragmentPerfilBinding binding;
    private PublicacionAdapter adapter;
    private PerfilViewModel vm;
    private PublicacionViewModel pubVM;

    @Override
    public View onCreateView(@NonNull LayoutInflater inf, ViewGroup c, Bundle b) {
        binding = FragmentPerfilBinding.inflate(inf, c, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // 1) Inicializa tu viewModel de perfil
        vm = new ViewModelProvider(this).get(PerfilViewModel.class);

        // 2) Inicializa el PublicacionViewModel compartido
        pubVM = new ViewModelProvider(requireActivity())
                .get(PublicacionViewModel.class);

        // 3) Configura el RecyclerView con GridLayoutManager
        binding.recyclerMisPosts.setLayoutManager(new GridLayoutManager(getContext(), 1));

        // 4) Crea el adapter con los 3 parámetros
        String idUsuarioActual = FirebaseAuth.getInstance().getCurrentUser().getUid();
        adapter = new PublicacionAdapter(new ArrayList<>(), pubVM, getViewLifecycleOwner(), idUsuarioActual, true);
        binding.recyclerMisPosts.setAdapter(adapter);

        // 5) Observa las publicaciones del perfil
        vm.getMisPublicaciones()
                .observe(getViewLifecycleOwner(), lista -> {
                    adapter.updateData(lista);
                });

        // Resto de tu lógica (editar perfil, cerrar sesión, bindUsuario…)
        vm.getUsuario().observe(getViewLifecycleOwner(), this::bindUsuario);
        binding.btnEditarPerfil.setOnClickListener(v2 ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_perfilFragment_to_editPerfilFragment)
        );
        binding.botonCerrarSesion.setOnClickListener(v3 -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(requireContext(), AuthActivity.class));
            requireActivity().finish();
        });
    }

    //Método para actualizar los datos del usuario
    private void bindUsuario(Usuario usuario) {
        binding.txtNombre.setText(usuario.getNombre());
        binding.txtBio.setText(usuario.getBio());
        binding.txtSeguidores.setText(String.valueOf(usuario.getSeguidores().size()));
        binding.txtSeguidos.setText(String.valueOf(usuario.getSeguidos().size()));
        Glide.with(this)
                .load(usuario.getUrlFotoPerfil())
                .placeholder(R.drawable.user)
                .into(binding.imgPerfil);
    }
}