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
    private final static String BOARDSIZE_KEY = "boardsize";
    public final static String BOARDSIZE_DEFAULT = "8";
    private final static String PLAYERUUID_KEY = "playeruuid";
    public final static String PLAYERUUID_DEFAULT = "";
    private final static String PLAYERNAME_KEY = "playername";
    public final static String PLAYERNAME_DEFAULT = "Player";
    private final static String PLAYERPASSWORD_KEY = "playerpassword";
    public final static String PLAYERPASSWORD_DEFAULT = "Password";
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        PreferenceFragment fragment = new PreferenceFragment();
        fragmentTransaction.replace(android.R.id.content, fragment);
        fragmentTransaction.commit();
    }
    public static void resetPreferences(Context context){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.commit();
    }
    public static int getBoardSize(Context context) {
        return Integer.parseInt(getKey(context, BOARDSIZE_KEY, BOARDSIZE_DEFAULT));
    }
    public static void setBoardsize(Context context, int size) {
        setKey(context, PreferenceActivity.BOARDSIZE_KEY, Integer.toString(size));
    }
    public static String getPlayerUUID(Context context) {
        return getKey(context, PLAYERUUID_KEY, PLAYERUUID_DEFAULT);
    }
    public static void setPlayerUUID(Context context, String uuid) {
        setKey(context, PreferenceActivity.PLAYERUUID_KEY, uuid);
    }
    public static String getPlayerName(Context context) {
        return getKey(context, PLAYERNAME_KEY, PLAYERNAME_DEFAULT);
    }
    public static void setPlayerName(Context context, String name) {
        setKey(context, PreferenceActivity.PLAYERNAME_KEY, name);
    }
    public static String getPlayerPassword(Context context) {
        return getKey(context, PLAYERPASSWORD_KEY, PLAYERPASSWORD_DEFAULT);
    }

    public static void setPlayerPassword(Context context, String password) {
        setKey(context, PreferenceActivity.PLAYERPASSWORD_KEY, password);
    }

    private static void setKey(Context context, String key, String value){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.commit();
    }

    private static String getKey(Context context, String key, String defaultValue){
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(key, defaultValue);
    }
}