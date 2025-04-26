package com.ferd.foodiegram.ui.home;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.*;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ferd.foodiegram.R;
import com.ferd.foodiegram.databinding.FragmentBuscarAmigoBinding;
import com.ferd.foodiegram.model.Usuario;
import com.ferd.foodiegram.viewmodel.BuscarAmigoViewModel;

import java.util.ArrayList;
import java.util.List;


public class BuscarAmigoFragment extends Fragment {
    private FragmentBuscarAmigoBinding binding;
    private BuscarAmigoViewModel vm;
    private AmigoAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inf, ViewGroup c, Bundle b) {
        binding = FragmentBuscarAmigoBinding.inflate(inf, c, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        super.onViewCreated(v, s);
        vm = new ViewModelProvider(this).get(BuscarAmigoViewModel.class);

        // RecyclerView
        RecyclerView rv = binding.recyclerBusqueda;
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        AmigoAdapter adapter = new AmigoAdapter(new ArrayList<>(), vm, getViewLifecycleOwner());
        rv.setAdapter(adapter);

        // Observa cambios
        vm.results.observe(getViewLifecycleOwner(), lista -> {
            adapter.updateData(lista);
        });

        // TextWatcher
        binding.editBusqueda.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s,int st,int c,int a){}
            @Override public void onTextChanged(CharSequence s,int st,int b,int c){
                String q = s.toString().trim();
                vm.setQuery(q);
            }
            @Override public void afterTextChanged(Editable e){}
        });
    }

    private void onUsers(List<Usuario> list) {
        adapter.updateData(list);
    }
}