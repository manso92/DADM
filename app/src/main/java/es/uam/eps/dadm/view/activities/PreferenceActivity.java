package es.uam.eps.dadm.view.activities;


import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import es.uam.eps.dadm.view.fragment.PreferenceFragment;

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
     * Clave para registrar las preferencias de audio
     */
    private final static String COLOR_SCHEME_KEY = "colorscheme";

    /**
     * Preferencias de audio por defecto para el usuario
     */
    public final static String COLOR_SCHEME_DEFAULT = "brown";

    /**
     * Clave para registrar las preferencias de audio
     */
    private final static String ONLINE_GAME_KEY = "onlinegame";

    /**
     * Preferencias de audio por defecto para el usuario
     */
    public final static boolean ONLINE_GAME_DEFAULT = true;

    /**
     * Clave para registrar el tamaño del tablero
     */
    private final static String BOARD_SIZE_KEY = "boardsize";

    /**
     * Tamaño del tablero por defecto para el usuario
     */
    public final static int BOARD_SIZE_DEFAULT = 8;

    /**
     * Clave para registrar el UUID del usuario
     */
    private final static String PLAYERUUID_KEY = "playeruuid";

    /**
     * UUID por defecto para el usuario
     */
    public final static String PLAYERUUID_DEFAULT = "00000000-0000-0000-0000-000000000000";

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
        Fragment fragment = new PreferenceFragment();
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

    public static boolean isOnline(Context context) {
        ConnectivityManager connMgr = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    public static boolean isLoggedIn(Context context) {
        return (!PreferenceActivity.getPlayerUUID(context).equals(PLAYERUUID_DEFAULT));
    }

    /**
     * Devuelve el valor del esquema de colores elegido por el usuario para el tablero
     * @param context Contexto desde el cual se quiere obtener el valor del usuario
     * @return Esquema de colores para el tablero
     */
    public static String getColorScheme(Context context) {
        return getKey(context, COLOR_SCHEME_KEY, COLOR_SCHEME_DEFAULT);
    }

    /**
     * Registra el esquema de colores que utilizará el tablero
     * @param context Contexto desde el cual se quiere registrar el valor
     * @param color Esquema de color que utilizará el tablero
     */
    public static void setColorScheme(Context context, String color) {
        setKey(context, COLOR_SCHEME_KEY, color);
    }

    /**
     * Devuelve el valor de las preferencias de juego online o offline
     * @param context Contexto desde el cual se quiere obtener el valor del usuario
     * @return Si se juega online o no
     */
    public static boolean getOnlineGame(Context context) {
        return getKey(context, ONLINE_GAME_KEY, ONLINE_GAME_DEFAULT);
    }

    /**
     * Registra si se juga online o no
     * @param context Contexto desde el cual se quiere registrar el valor
     * @param online Si el juego es online o no
     */
    public static void setOnlineGame(Context context, boolean online) {
        setKey(context, BOARD_SIZE_KEY, online);
    }

    /**
     * Devuelve el valor del tamaño del tablero
     * @param context Contexto desde el cual se quiere obtener el valor del usuario
     * @return Tamaño del tablero
     */
    public static int getSize(Context context) {
        return Integer.parseInt(getKey(context, BOARD_SIZE_KEY, Integer.toString(BOARD_SIZE_DEFAULT)));
    }

    /**
     * Registra el tamaño del tablero en las preferencias de usuario
     * @param context Contexto desde el cual se quiere registrar el valor
     * @param size Tamaño del tablero
     */
    public static void setSize(Context context, int size) {
        setKey(context, BOARD_SIZE_KEY, Integer.toString(size));
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
     * Registra una clave en las preferencias de usuario
     * @param context Contexto desde el cual se quiere registrar el valor
     * @param key Clave que se quiere registrar
     * @param value Valor que se le quiere dar a la clave
     */
    private static void setKey(Context context, String key, boolean value){
        // Obtenemos una referencia de las preferencias de usuario
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        // Creamos una referencia del editor de claves
        SharedPreferences.Editor editor = sharedPreferences.edit();
        // Registramos la clave y commiteamos el cambio
        editor.putBoolean(key, value);
        editor.commit();
    }

    /**
     * Registra una clave en las preferencias de usuario
     * @param context Contexto desde el cual se quiere registrar el valor
     * @param key Clave que se quiere registrar
     * @param value Valor que se le quiere dar a la clave
     */
    private static void setKey(Context context, String key, int value){
        // Obtenemos una referencia de las preferencias de usuario
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        // Creamos una referencia del editor de claves
        SharedPreferences.Editor editor = sharedPreferences.edit();
        // Registramos la clave y commiteamos el cambio
        editor.putInt(key, value);
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

    /**
     * Obtiene una clave de las preferencias de usuario
     * @param context Contexto desde el cual se pide el valor
     * @param key Clave que se está pidiendo
     * @param defaultValue Valor por defecto en caso de no encontrarse la clave
     * @return Boolean asociado a la clave que se ha provisto
     */
    private static boolean getKey(Context context, String key, boolean defaultValue){
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(key, defaultValue);
    }

    /**
     * Obtiene una clave de las preferencias de usuario
     * @param context Contexto desde el cual se pide el valor
     * @param key Clave que se está pidiendo
     * @param defaultValue Valor por defecto en caso de no encontrarse la clave
     * @return Boolean asociado a la clave que se ha provisto
     */
    private static int getKey(Context context, String key, int defaultValue){
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getInt(key, defaultValue);
    }
}