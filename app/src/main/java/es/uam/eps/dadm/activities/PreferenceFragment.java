package es.uam.eps.dadm.activities;

import android.os.Bundle;
import es.uam.eps.dadm.R;

public class PreferenceFragment extends android.preference.PreferenceFragment {
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
    }
}