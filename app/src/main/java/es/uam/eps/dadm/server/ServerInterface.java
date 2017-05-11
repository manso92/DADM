package es.uam.eps.dadm.server;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

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

    /**
     * Tag para escribir en el log
     */
    public static final String DEBUG = "Damas.SInterface";

    // PARÁMETROS PARA LOS SCRIPTS DEL SERVIDOR
    private static final String PLAYER_NAME_TAG = "playername";
    private static final String PLAYER_PASSWORD_TAG = "playerpassword";
    private static final String PLAYER_ID_TAG = "playerid";
    private static final String GOOGLE_CLOUT_TAG = "gcmregid";
    private static final String LOGIN_TAG = "login";
    private static final String GAMEID_TAG = "gameid";
    static final String ROUND_ID_TAG = "roundid";
    static final String ROUND_PLAYER_NUMBER_TAG = "numberofplayers";
    static final String ROUND_DATE_TAG = "dateevent";
    static final String ROUND_PLAYER_NAMES_TAG = "playernames";
    static final String ROUND_TURN_TAG = "turn";
    static final String ROUND_CODEBOARD_TAG = "codedboard";
    static final String MSG_FROM_TAG = PLAYER_ID_TAG;
    static final String MSG_PLAYER_TAG = PLAYER_ID_TAG;
    static final String MSG_ROUND_TAG = ROUND_ID_TAG;
    static final String MSG_TO_PLAYER_TAG = "to";
    static final String MSG_TO_ROUND_TAG = "toround";
    static final String MSG_DATA_TAG = "msg";
    static final String MSG_DATE_TAG = "fromdate";

    // URL BASE DONDE SE ENCUENTRAN TODOS LOS SCRIPTS
    private static final String BASE_URL = "http://ptha.ii.uam.es/dadm2017/";

    // ACCOUNTS SCRIPTS
    private static final String ACCOUNT_PHP =  BASE_URL + "account.php";

    // ROUND LIST SCRIPTS
    private static final String ROUNDS_OPEN_PHP = BASE_URL + "openrounds.php";
    private static final String ROUNDS_ACTIVE_PHP = BASE_URL + "activerounds.php";
    private static final String ROUNDS_FINISHED_PHP = BASE_URL + "finishedrounds.php";

    // ROUND MANAGE SCRIPTS
    private static final String ROUND_NEW_PHP = BASE_URL + "newround.php";
    private static final String ROUND_ADD_PLAYER_PHP = BASE_URL + "addplayertoround.php";
    private static final String ROUND_REMOVE_PLAYER_PHP = BASE_URL + "removeplayerfromround.php";

    // GAME SCRIPTS
    private static final String GAME_TURN_PHP = BASE_URL + "ismyturn.php";
    private static final String GAME_HISTORY_PHP = BASE_URL + "roundhistory.php";
    private static final String GAME_MOVEMENT_PHP = BASE_URL + "newmovement.php";

    // MESSAGE SCRIPTS
    private static final String MESSAGE_SEND_PHP = BASE_URL + "sendmsg.php";
    private static final String MESSAGE_GET_PHP = BASE_URL + "getmsgs.php";

    /**
     * Identificador del juego en el servidor
     */
    private static final int GAME_ID = 17;

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
     * Función que nos devuleve la instancia de la interfaz con el servidor
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
                Map<String, String> params = new HashMap<String, String>();
                params.put(PLAYER_NAME_TAG, user);
                params.put(PLAYER_PASSWORD_TAG, password);

                if (regid != null && !regid.isEmpty()) params.put(GOOGLE_CLOUT_TAG, regid);
                if (!login) params.put(LOGIN_TAG, "");

                return params;
            }
        };

        // Añadimos la request a la cola
        queue.add(r);
    }

    /**
     * Busca todas las partidas abiertas para un jugador determinado
     * @param playerid Identificador del usuario (uuid)
     * @param callback Calback a ejecutar en caso de que todo se produzca de forma correcta
     * @param errorCallback Callback a ejecutar en caso de que haya un error al ejecutar la petición
     */
    public void getOpenRounds(final String playerid, Response.Listener<JSONArray> callback,
                              ErrorListener errorCallback) {
        // Creamos la URL con todos los parámetros mediante GET
        String url = ROUNDS_OPEN_PHP + "?" +
                GAMEID_TAG  + "=" + GAME_ID + "&" +
                PLAYER_ID_TAG + "=" + playerid;
        Log.d(DEBUG, url);

        // Creamos una request para recibir un JsonArray y añadimos la request a la cola
        JsonArrayRequest r = new JsonArrayRequest(url, callback, errorCallback);
        queue.add(r);
    }

    /**
     * Busca todas las partidas que tengamos empezadas
     * @param playerid Identificador del usuario (uuid)
     * @param callback Calback a ejecutar en caso de que todo se produzca de forma correcta
     * @param errorCallback Callback a ejecutar en caso de que haya un error al ejecutar la petición
     */
    public void getActiveRounds(final String playerid, Response.Listener<JSONArray> callback,
                                ErrorListener errorCallback) {
        // Creamos la URL con todos los parámetros mediante GET
        String url = ROUNDS_ACTIVE_PHP + "?" +
                GAMEID_TAG  + "=" + GAME_ID + "&" +
                PLAYER_ID_TAG + "=" + playerid;
        Log.d(DEBUG, url);

        // Creamos una request para recibir un JsonArray y añadimos la request a la cola
        JsonArrayRequest r = new JsonArrayRequest(url, callback, errorCallback);
        queue.add(r);
    }

    /**
     * Busca todas las partidas que tengamos terminadas
     * @param playerid Identificador del usuario (uuid)
     * @param callback Calback a ejecutar en caso de que todo se produzca de forma correcta
     * @param errorCallback Callback a ejecutar en caso de que haya un error al ejecutar la petición
     */
    public void getFinishedRounds(final String playerid, Response.Listener<JSONArray> callback,
                                ErrorListener errorCallback) {
        // Creamos la URL con todos los parámetros mediante GET
        String url = ROUNDS_FINISHED_PHP + "?" +
                GAMEID_TAG  + "=" + GAME_ID + "&" +
                PLAYER_ID_TAG + "=" + playerid;
        Log.d(DEBUG, url);

        // Creamos una request para recibir un JsonArray y añadimos la request a la cola
        JsonArrayRequest r = new JsonArrayRequest(url, callback, errorCallback);
        queue.add(r);
    }

    /**
     * Crea una nueva partida para el jugador indicado
     * @param playerid Identificador del usuario (uuid)
     * @param callback Calback a ejecutar en caso de que todo se produzca de forma correcta
     * @param errorCallback Callback a ejecutar en caso de que haya un error al ejecutar la petición
     */
    public void newRound(String playerid, String codedboard, Response.Listener<String>
            callback, ErrorListener errorCallback) {
        // Creamos la URL con todos los parámetros mediante GET
        String url = ROUND_NEW_PHP + "?" +
                GAMEID_TAG + "=" + GAME_ID + "&" +
                PLAYER_ID_TAG + "=" + playerid + "&" +
                ROUND_CODEBOARD_TAG + "=" + codedboard;
        Log.d(DEBUG, url);

        // Creamos una request para recibir una cadena con el identificador y añadimos la request a la cola
        StringRequest r = new StringRequest(url, callback, errorCallback);
        queue.add(r);
    }

    /**
     * Añade un jugador a una partida dada
     * @param roundid Identificador de la partida
     * @param playerid Identificador del usuario (uuid)
     * @param callback Calback a ejecutar en caso de que todo se produzca de forma correcta
     * @param errorCallback Callback a ejecutar en caso de que haya un error al ejecutar la petición
     */
    public void addPlayerToRound(int roundid, String playerid, Response.Listener<String>
            callback, ErrorListener errorCallback) {
        // Creamos la URL con todos los parámetros mediante GET
        String url = ROUND_ADD_PLAYER_PHP + "?" +
                ROUND_ID_TAG + "=" + roundid + "&" +
                PLAYER_ID_TAG + "=" + playerid;
        Log.d(DEBUG, url);

        // Creamos una request para recibir una cadena con el número de jugadores y añadimos la request a la cola
        StringRequest r = new StringRequest(url, callback, errorCallback);
        queue.add(r);
    }


    /**
     * Elimina un jugador a una partida dada
     * @param roundid Identificador de la partida
     * @param playerid Identificador del usuario (uuid)
     * @param callback Calback a ejecutar en caso de que todo se produzca de forma correcta
     * @param errorCallback Callback a ejecutar en caso de que haya un error al ejecutar la petición
     */
    public void removePlayerToRound(int roundid, String playerid, Response.Listener<String>
            callback, ErrorListener errorCallback) {
        // Creamos la URL con todos los parámetros mediante GET
        String url = ROUND_REMOVE_PLAYER_PHP + "?" +
                ROUND_ID_TAG + "=" + roundid + "&" +
                PLAYER_ID_TAG + "=" + playerid;
        Log.d(DEBUG, url);

        // Creamos una request para recibir una cadena con el número de jugadores y añadimos la request a la cola
        StringRequest r = new StringRequest(url, callback, errorCallback);
        queue.add(r);
    }

    /**
     * Busca si el turno en la ronda es del jugador
     * @param roundid Identificador de la ronda
     * @param playerid Identificador del usuario (uuid)
     * @param callback Calback a ejecutar en caso de que todo se produzca de forma correcta
     * @param errorCallback Callback a ejecutar en caso de que haya un error al ejecutar la petición
     */
    public void isMyTurn(final int roundid, final String playerid,
                         Response.Listener<JSONObject> callback, ErrorListener errorCallback) {
        // Creamos la URL con todos los parámetros mediante GET
        String url = GAME_TURN_PHP + "?" +
                ROUND_ID_TAG  + "=" + roundid + "&" +
                PLAYER_ID_TAG + "=" + playerid;
        Log.d(DEBUG, url);

        // Creamos una request para recibir un JsonObject y añadimos la request a la cola
        JsonObjectRequest r = new JsonObjectRequest(url, null, callback, errorCallback);
        queue.add(r);
    }

    /**
     * Busca el historial completo de una partida
     * @param roundid Identificador de la ronda
     * @param playerid Identificador del usuario (uuid)
     * @param callback Calback a ejecutar en caso de que todo se produzca de forma correcta
     * @param errorCallback Callback a ejecutar en caso de que haya un error al ejecutar la petición
     */
    public void roundHistory(final int roundid, final String playerid,
                         Response.Listener<JSONArray> callback, ErrorListener errorCallback) {
        // Creamos la URL con todos los parámetros mediante GET
        String url = GAME_HISTORY_PHP + "?" +
                ROUND_ID_TAG  + "=" + roundid + "&" +
                PLAYER_ID_TAG + "=" + playerid;
        Log.d(DEBUG, url);

        // Creamos una request para recibir un JsonArray y añadimos la request a la cola
        JsonArrayRequest r = new JsonArrayRequest(url, callback, errorCallback);
        queue.add(r);
    }

    /**
     * Añade un nuevo movimiento a una partida
     * @param roundid Identificador de la ronda
     * @param playerid Identificador del usuario (uuid)
     * @param codedboard Tablero tras el movimiento
     * @param callback Calback a ejecutar en caso de que todo se produzca de forma correcta
     * @param errorCallback Callback a ejecutar en caso de que haya un error al ejecutar la petición
     */
    public void newMovement(int roundid, String playerid, String codedboard,
                            Response.Listener<String> callback, ErrorListener errorCallback) {
        // Creamos la URL con todos los parámetros mediante GET
        String url = GAME_MOVEMENT_PHP + "?" +
                ROUND_ID_TAG  + "=" + roundid + "&" +
                PLAYER_ID_TAG + "=" + playerid + "&" +
                ROUND_CODEBOARD_TAG + "=" + codedboard;
        Log.d(DEBUG, url);

        // Creamos una request para recibir una cadena con el tablero y añadimos la request a la cola
        StringRequest r = new StringRequest(url, callback, errorCallback);
        queue.add(r);
    }

    /**
     * Envía un mensaje a una ronda o a un usuario
     * @param from UUID del usuario que manda el mensaje
     * @param to Usuario al que se manda un mensaje o id de la ronda a la que se manda
     * @param msg Mensaje que se manda
     * @param round Si el mensaje que se manda es a una ronda o no
     * @param callback Calback a ejecutar en caso de que todo se produzca de forma correcta
     * @param errorCallback Callback a ejecutar en caso de que haya un error al ejecutar la petición
     */
    public void sendMessages(String from, String to, String msg, boolean round,
                            Response.Listener<String> callback, ErrorListener errorCallback) {
        // Creamos la URL con todos los parámetros mediante GET
        String url = MESSAGE_SEND_PHP + "?" +
                MSG_FROM_TAG  + "=" + from + "&" +
                MSG_DATA_TAG + "=" + msg + "&" ;
        if (round) url += MSG_TO_ROUND_TAG + "=" + to;
        else       url += MSG_TO_PLAYER_TAG + "=" + to;
        Log.d(DEBUG, url);

        // Creamos una request para recibir un JSONArray y añadimos la request a la cola
        StringRequest r = new StringRequest(url, callback, errorCallback);
        queue.add(r);
    }

    /**
     * Recibe todos los mensajes enviados a una ronda o a un usuario
     * @param id UUID del usuario o id de la ronda
     * @param round Si se reciben mensajes de una ronda o no
     * @param callback Calback a ejecutar en caso de que todo se produzca de forma correcta
     * @param errorCallback Callback a ejecutar en caso de que haya un error al ejecutar la petición
     */
    public void getMessages(String id, boolean round, Response.Listener<JSONArray> callback,
                            ErrorListener errorCallback) {
        // Creamos la URL con todos los parámetros mediante GET
        String url = MESSAGE_GET_PHP + "?" ;
        if (round) url += MSG_ROUND_TAG + "=" + id;
        else       url += MSG_PLAYER_TAG + "=" + id;
        Log.d(DEBUG, url);

        // Creamos una request para recibir una cadena con el tablero y añadimos la request a la cola
        JsonArrayRequest r = new JsonArrayRequest(url, callback, errorCallback);
        queue.add(r);
    }
}
