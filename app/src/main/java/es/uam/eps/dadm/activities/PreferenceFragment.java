package es.uam.eps.dadm.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;

import es.uam.eps.dadm.R;

/**
 * PreferenceFragment muestra las preferencias modificables de la app
 *
 * @author Pablo Manso
 * @version 12/04/2017
 */
public class PreferenceFragment extends android.preference.PreferenceFragment {

    /**
     * Crea todo lo necesario para la correcta ejecución del fragmento
     * @param savedInstanceState Pares clave valor que nos pasa la actividad que nos invoca
     */
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);

        // Asignamos a las dos preferencias que son vínculos
        findPreference("scores").setIntent(new Intent(this.getActivity(), ScoresActivity.class));
        findPreference("help").setIntent(new Intent(this.getActivity(), HelpActivity.class));
    }
}