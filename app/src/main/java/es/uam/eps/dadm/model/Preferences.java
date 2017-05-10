package es.uam.eps.dadm.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import es.uam.eps.dadm.view.activities.PreferenceActivity;

/**
 * Acceso rápido a las preferencias de la actividad
 * @author Pablo Manso
 * @version 10/05/2017
 */

public class Preferences {

    /**
     * Tag para escribir en el log
     */
    public static final String DEBUG = "Damas.Preferences";

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
    public final static boolean ONLINE_GAME_DEFAULT = false;

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
     * Clave para registrar la contraseña de usuario
     */
    private final static String PLAYERPASSWORD_KEY = "playerpassword";

    /**
     * Nombre por defecto para el usuario
     */
    public final static String PLAYERPASSWORD_DEFAULT = "";

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


    public static boolean isLoggedIn(Context context) {
        return (!getPlayerUUID(context).equals(PLAYERUUID_DEFAULT));
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
        setKey(context, PLAYERUUID_KEY, uuid);
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
        setKey(context, PLAYERNAME_KEY, name);
    }

    /**
     * Devuelve el valor del hash de la password de usuario que está registrado en las preferencias
     * @param context Contexto desde el cual se quiere obtener el valor del usuario
     * @return Hash de la password de usuario registrado | Default value para la password de usuario
     */
    public static String getPlayerPassword(Context context) {
        return getKey(context, PLAYERPASSWORD_KEY, PLAYERPASSWORD_DEFAULT);
    }

    /**
     * Registra la contraseña del usuario logueado en las preferencias de usuario
     * @param context Contexto desde el cual se quiere registrar el valor
     * @param password Contraseña del usuario
     */
    public static void setPlayerPassword(Context context, String password) {
        setKey(context, PLAYERPASSWORD_KEY, password);
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
