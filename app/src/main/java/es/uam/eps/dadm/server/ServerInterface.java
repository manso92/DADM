package es.uam.eps.dadm.server;

import android.content.Context;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import java.util.HashMap;
import java.util.Map;

/**
 * ServerInterface se encargará de hacer transaparentes las invocaciones al servidor, de modo que
 * proveerá funciones con los parámetros de cada script a ejecutar y se encargará de realizar
 * las peticiones y de gestionar las respuestas.
 *
 * @author Pablo Manso
 * @version 05/05/2017
 */
public class ServerInterface {

    //TAGS PARA EL SERVIDOR
    private static final String PLAYER_NAME_TAG = "playername";
    private static final String PLAYER_PASSWORD_TAG = "playerpassword";
    private static final String PLAYER_ID_TAG = "playerid";
    private static final String GOOGLE_CLOUT_TAG = "gcmregid";
    private static final String LOGIN_TAG = "login";
    private static final String ROUND_ID_TAG = "roundid";
    private static final String CODEBOARD_TAG = "codedboard";



    // WEBS DONDE INTERACTUAR
    private static final String DEBUG = "DEBUG";
    private static final String BASE_URL = "http://ptha.ii.uam.es/dadm2017/";
    private static final String ACCOUNT_PHP =  BASE_URL + "account.php";
    private static final String IS_MY_TURN_PHP = BASE_URL + "ismyturn.php";
    private static final String OPEN_ROUNDS_PHP = BASE_URL + "openrounds.php";
    private static final String ACTIVE_ROUNDS_PHP = BASE_URL + "activerounds.php";
    private static final String ROUND_HISTORY_PHP = BASE_URL + "roundhistory.php";
    private static final String NEW_MOVEMENT_PHP = BASE_URL + "newmovement.php";
    private static final String NEW_ROUND_PHP = BASE_URL + "newround.php";
    private static final String ADD_PLAYER_TO_ROUND_PHP = BASE_URL + "addplayertoround.php";

    /**
     * Identificador del juego en el servidor
     */
    public static final int GAME_ID = 27;

    /**
     * Queue de peticiones con la que mandaremos mensajes al servidor
     */
    private RequestQueue queue;

    /**
     * Instancia de uno mismo para implementar el singleton
     */
    private static ServerInterface serverInteraface;
    
    /**
     * Constructor de la clase privado para garantizar el singleton
     * @param context Contexto desde el que se crea el servidor
     */
    private ServerInterface(Context context) {
        // Creamos una cola de peticiones de Volley
        queue = Volley.newRequestQueue(context.getApplicationContext());
    }

    /**
     * Función que nos devuleve la instancia del servidor
     * @param context Contexto desde el que se crea el servidor
     * @return Instancia del servidor creado
     */
    public static ServerInterface getServer(Context context) {
        // Si no hemos creado aún la interfaz, la creamos y devolvemos la instnacia
        if (serverInteraface == null)
            serverInteraface = new ServerInterface(context);

        return serverInteraface;
    }


    /**
     * Función que loguea o registra un usuario en el servidor
     * @param user Nombre de usuario
     * @param password Contraseña de usuario
     * @param regid Clave de google cloud messaging
     * @param login Nos indicará si es un login (true) o un registro (register)
     * @param callback Calback a ejecutar en caso de que todo se produzca de forma correcta
     * @param errorCallback Callback a ejecutar en caso de que haya un error al ejecutar la petición
     */
    public void login(final String user, final String password, final String regid, final boolean login,
                      Response.Listener<String> callback, ErrorListener errorCallback){

        // Creamos una request para hacer login o registrarse en el servidor
        StringRequest r = new StringRequest(Request.Method.POST, ACCOUNT_PHP, callback, errorCallback)
        {
            /**
             * Crea un conjunto de pares clave valor que se enviarán al servidor
             * @return Conjunto pares clave valor que se enviarán al servidor
             */
            @Override
            protected Map<String, String> getParams() {
                // Creamos un contenedor para las claves
                Map<String, String> params = new HashMap<String, String>();
                // Colocamos el usuario y la contraseña en el contenedor
                params.put(PLAYER_NAME_TAG, user);
                params.put(PLAYER_PASSWORD_TAG, password);
                // Si tenemos clave de Google Cloud Messagging la adjuntamos
                if (regid != null && !regid.isEmpty()) params.put(GOOGLE_CLOUT_TAG, regid);
                // Si se trata de un login, se lo indicamos
                if (!login) params.put(LOGIN_TAG, "");

                // Devolvemos los parámetros
                return params;
            }
        };
        // Añadimos la request a la cola
        queue.add(r);
    }
}