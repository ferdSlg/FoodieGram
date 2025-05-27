package com.ferd.foodiegram.ui.home;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ferd.foodiegram.R;
import com.ferd.foodiegram.utilidades.Resource;
import com.ferd.foodiegram.viewmodel.PublicacionViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;

public class ComentariosFragment extends Fragment {
    private PublicacionViewModel viewModel;
    private ComentarioAdapter adapter;
    private String postId;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Infla el layout que contiene RecyclerView y barra de envío
        return inflater.inflate(R.layout.fragment_comentarios, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //1 Obtengo el ViewModel compartido con la Activity
        viewModel = new ViewModelProvider(requireActivity())
                .get(PublicacionViewModel.class);

        //2 Preparo el RecyclerView y su adapter (una única vez)
        RecyclerView rvComentarios = view.findViewById(R.id.rvComentarios);

        LinearLayoutManager lm = new LinearLayoutManager(getContext());
        lm.setStackFromEnd(true);      //apila los ítems hacia abajo
        lm.setReverseLayout(false);

        rvComentarios.setLayoutManager(lm);
        adapter = new ComentarioAdapter(new ArrayList<>());
        rvComentarios.setAdapter(adapter);

        //3 Recupero el postId de los argumentos
        Bundle args = getArguments();
        if (args != null) {
            postId = args.getString("postId");
        }

        //4 Observo los comentarios en tiempo real y actualizo el adapter
        if (postId != null) {
            viewModel.getComments(postId).observe(getViewLifecycleOwner(), comentarios -> {
                adapter.updateData(comentarios);
                //bajar al último comentario, pero colocándolo en el fondo:
                rvComentarios.post(() -> {
                    LinearLayoutManager lm2 = (LinearLayoutManager) rvComentarios.getLayoutManager();
                    if (lm2 != null) {
                        lm2.scrollToPositionWithOffset(comentarios.size() - 1, 0);
                    }
                });
            });
        }

        //5 Configuro el formulario para enviar nuevos comentarios
        TextInputEditText etComentario = view.findViewById(R.id.etComentario);
        MaterialButton btnEnviar = view.findViewById(R.id.btnEnviarComentario);
        btnEnviar.setOnClickListener(v -> {
            String texto = etComentario.getText() != null
                    ? etComentario.getText().toString().trim()
                    : "";
            if (!TextUtils.isEmpty(texto) && postId != null) {
                viewModel.addComment(postId, texto)
                        .observe(getViewLifecycleOwner(), res -> {
                            if (res.status == Resource.Status.SUCCESS) {
                                //Limpio el campo al enviar correctamente
                                etComentario.setText("");
                            }
                        });
            }
        });
    }
}
