package es.uam.eps.dadm.activities;


import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

import es.uam.eps.dadm.R;

public class PreferenceActivity extends AppCompatActivity {
    public final static String BOARDSIZE_KEY = "boardsize";
    public final static String BOARDSIZE_DEFAULT = "0";
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager
                .beginTransaction();
        PreferenceFragment fragment = new PreferenceFragment();
        fragmentTransaction.replace(android.R.id.content, fragment);
        fragmentTransaction.commit();
    }
    public static String getBoardSize(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(BOARDSIZE_KEY, BOARDSIZE_DEFAULT);
    }
    public static void setBoardsize(Context context, int size) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(PreferenceActivity.BOARDSIZE_KEY, size);
        editor.commit();
    }
}