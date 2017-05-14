package com.manso92.damas.view.activities;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.manso92.damas.view.fragment.PreferenceFragment;
import com.manso92.damas.R;

/**
 * PreferenceActivity muestra una actividad desde la que modificar todas las preferencias de usuario.
 * Asímismo provee de las funciones necesarias para modificar y obtener las diferentes claves
 * que se van a guardar en la app.
 *
 * @author Pablo Manso
 * @version 12/04/2017
 */
public class PreferenceActivity extends AppCompatActivity {

    /**
     * Tag para escribir en el log
     */
    public static final String DEBUG = "Damas.PreferenceAct";

    /**
     * Crea todo lo necesario para la correcta ejecución de la actividad
     * @param savedInstanceState Pares clave valor que nos pasa la actividad que nos invoca
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Colocamos el layout del fragment
        setContentView(R.layout.activity_fragment);
        // Creamos un fragment manager para colocar el PreferenceFragment
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Fragment fragment = new PreferenceFragment();
        // Cambiamos el contenido por defecto pro nuestro fragmento
        fragmentTransaction.replace(android.R.id.content, fragment);
        fragmentTransaction.commit();
    }

}