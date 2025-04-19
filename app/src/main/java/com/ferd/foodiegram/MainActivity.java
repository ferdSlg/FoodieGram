package com.ferd.foodiegram;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.ferd.foodiegram.ui.LoginFragment;
import com.ferd.foodiegram.ui.home.HomeFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    private BottomNavigationView botonNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        botonNav = findViewById(R.id.bottomNavigationView);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new LoginFragment())
                    .commit();
        }

        botonNav.setOnItemSelectedListener(item -> {
            Fragment seleccionado = null;
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                seleccionado = new HomeFragment();
            } else if (id == R.id.nav_buscar) {
                // seleccionado = new BuscarFragment();
            } else if (id == R.id.nav_crear) {
                // seleccionado = new CrearPublicacionFragment();
            } else if (id == R.id.nav_perfil) {
                // seleccionado = new PerfilFragment();
            }

            if (seleccionado != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, seleccionado)
                        .commit();
            }

            return true;
        });
    }

}