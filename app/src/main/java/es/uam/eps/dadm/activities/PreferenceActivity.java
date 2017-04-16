package es.uam.eps.dadm.activities;


import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

import es.uam.eps.dadm.R;

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
     * Clave para registrar el UUID del usuario
     */
    private final static String PLAYERUUID_KEY = "playeruuid";

    /**
     * UUID por defecto para el usuario
     */
    public final static String PLAYERUUID_DEFAULT = "280f83bc-cc34-4744-b20c-b70f0765c846";

    /**
     * Clave para registrar el nombre de usuario
     */
    private final static String PLAYERNAME_KEY = "playername";

    /**
     * Nombre por defecto para el usuario
     */
    public final static String PLAYERNAME_DEFAULT = "Player";

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
        PreferenceFragment fragment = new PreferenceFragment();
        // Cambiamos el contenido por defecto pro nuestro fragmento
        fragmentTransaction.replace(android.R.id.content, fragment);
        fragmentTransaction.commit();
    }

    /**
     * Limpia las preferencias de usuario
     * @param context Contexto desde el cual se quieren modificar las preferencias
     */
    public static void resetPreferences(Context context){
        // Obtenemos una referencia de las preferencias de usuario
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        // Creamos una referencia del editor de claves
        SharedPreferences.Editor editor = sharedPreferences.edit();
        // Limpia las preferencias y commiteamos el cambio
        editor.clear();
        editor.commit();
    }

    /**
     * Devuelve el valor del identificador de usuario que está registrado en las preferencias de usuario
     * @param context Contexto desde el cual se quiere obtener el valor del usuario
     * @return Identificador de usuario registrado | Default value para el identificador de usuario
     */
    public static String getPlayerUUID(Context context) {
        return getKey(context, PLAYERUUID_KEY, PLAYERUUID_DEFAULT);
    }

    /**
     * Registra el identificador del usuario logueado en las preferencias de usuario
     * @param context Contexto desde el cual se quiere registrar el valor
     * @param uuid Identificador de usuario
     */
    public static void setPlayerUUID(Context context, String uuid) {
        setKey(context, PreferenceActivity.PLAYERUUID_KEY, uuid);
    }

    /**
     * Devuelve el valor del nombre de usuario que está registrado en las preferencias de usuario
     * @param context Contexto desde el cual se quiere obtener el valor del usuario
     * @return Nombre de usuario registrado | Default value para el nombre de usuario
     */
    public static String getPlayerName(Context context) {
        return getKey(context, PLAYERNAME_KEY, PLAYERNAME_DEFAULT);
    }

    /**
     * Registra el nombre del usuario logueado en las preferencias de usuario
     * @param context Contexto desde el cual se quiere registrar el valor
     * @param name Nombre de usuario
     */
    public static void setPlayerName(Context context, String name) {
        setKey(context, PreferenceActivity.PLAYERNAME_KEY, name);
    }

    /**
     * Registra una clave en las preferencias de usuario
     * @param context Contexto desde el cual se quiere registrar el valor
     * @param key Clave que se quiere registrar
     * @param value Valor que se le quiere dar a la clave
     */
    private static void setKey(Context context, String key, String value){
        // Obtenemos una referencia de las preferencias de usuario
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        // Creamos una referencia del editor de claves
        SharedPreferences.Editor editor = sharedPreferences.edit();
        // Registramos la clave y commiteamos el cambio
        editor.putString(key, value);
        editor.commit();
    }

    /**
     * Obtiene una clave de las preferencias de usuario
     * @param context Contexto desde el cual se pide el valor
     * @param key Clave que se está pidiendo
     * @param defaultValue Valor por defecto en caso de no encontrarse la clave
     * @return String asociado a la clave que se ha provisto
     */
    private static String getKey(Context context, String key, String defaultValue){
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(key, defaultValue);
    }
}