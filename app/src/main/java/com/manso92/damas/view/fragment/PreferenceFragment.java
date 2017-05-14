package com.manso92.damas.view.fragment;

import android.content.Intent;
import android.os.Bundle;

import com.manso92.damas.R;
import com.manso92.damas.view.activities.HelpActivity;

/**
 * PreferenceFragment muestra las preferencias modificables de la app
 *
 * @author Pablo Manso
 * @version 12/04/2017
 */
public class PreferenceFragment extends android.preference.PreferenceFragment {

    /**
     * Tag para escribir en el log
     */
    public static final String DEBUG = "Damas.PreferenceFrag";

    /**
     * Crea todo lo necesario para la correcta ejecución del fragmento
     * @param savedInstanceState Pares clave valor que nos pasa la actividad que nos invoca
     */
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
    }
}