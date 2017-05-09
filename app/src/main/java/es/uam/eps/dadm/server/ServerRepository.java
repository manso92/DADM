package es.uam.eps.dadm.server;

import android.content.Context;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import es.uam.eps.dadm.model.Preferences;
import es.uam.eps.dadm.model.Round;
import es.uam.eps.dadm.model.RoundRepository;
import es.uam.eps.multij.ExcepcionJuego;

import static es.uam.eps.dadm.model.Round.*;
import static es.uam.eps.dadm.model.Round.Type.*;

/**
 * Clase controladora del servidor de usuarios y partidas. Implementa la interfaz
 * {@link RoundRepository} para que funcione con el resto de la lógica de la aplicación
 *
 * @author Pablo Manso
 * @version 05/05/2017
 */
public class ServerRepository implements RoundRepository {

    /**
     * Tag de debug para escribir en el log
     */
    private static final String DEBUG = "ServerRepository";

    /**
     * Instancia de nosotros mismos para implementar el singleton
     */
    private static ServerRepository repository;

    /**
     * Contexto de la aplicación para realizar la aplicación
     */
    private final Context context;

    /**
     * Instancia de la interfaz del servidor al que mandaremos las peticiones
     */
    private ServerInterface is;

    /**
     * Constructor de la clase privado para garantizar el singleton
     * @param context Contexto desde el que se crea el repositorio
     */
    private ServerRepository(Context context) {
        // Cogemos el contexto de la aplicación y creamos una instnacia de la interfaz con el servidor
        this.context = context.getApplicationContext();
        this.is = ServerInterface.getServer(context);
    }

    /**
     * Función que nos devuleve la instancia del servidor
     * @param context Contexto desde el que se crea el servidor
     * @return Instancia del servidor creado
     */
    public static ServerRepository getInstance(Context context) {
        // Si no hemos creado aún el repositorio, la creamos y devolvemos la instnacia
        if (repository == null)
            repository = new ServerRepository(context.getApplicationContext());
        return repository;
    }

    @Override
    public void open() throws Exception {}
    @Override
    public void close() {}

    /**
     * Loguea o registra un usuario en el servidor
     * @param playerName Nombre de usuario
     * @param password Contraseña de usuario
     * @param login Si es true es un login, si es false será un registro
     * @param callback Callback a ejecutar tras la correcta o incorreta ejecución de la función
     */
    public void loginOrRegister(final String playerName, String password, boolean login,
                                final LoginRegisterCallback callback) {
        // Registramos un listener para manejar el correcto funcionamiento de la petición en el servidor
        Response.Listener<String> response = new Response.Listener<String>() {
            @Override
            public void onResponse(String result) {
            // Cogemos el uuid devuelto por el servidor
            String uuid = result.trim();

            // Comprobamos si es un uuid correcto o no e invocamos al callback en consecuencia
            if (uuid.equals("-1") || uuid.length() < 10)
                callback.onError("Error loggin in user " + playerName);
            else {
                callback.onLogin(uuid);
                Log.d(DEBUG, "Logged in: " + uuid);
            }
            }
        };
        // Registramos un listener para manejar el mal funcionamiento de la petición en el servidor
        Response.ErrorListener error = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Invocamos el error en el callback con el error que nos indica volley
                callback.onError(error.getLocalizedMessage());
            }
        };
        // Hacemos login contra la interfaz del servidor
        is.login(playerName, password, null, login, response, error);
    }

    /**
     * Función que nos logueará en el servidor
     * @param playerName Nombre del jugador
     * @param password Contraseña del jugador
     * @param callback Callback que se ejecutará como respuesta al login
     */
    @Override
    public void login(String playerName, String password,
                      final RoundRepository.LoginRegisterCallback callback) {
        loginOrRegister(playerName, password, false, callback);
    }

    /**
     * Registra un nuevo usuario en el servidor
     * @param playerName Nombre del jugador
     * @param password Contraseña del jugador
     * @param callback Callback que se ejecutará como respuesta al registro
     */
    @Override
    public void register(String playerName, String password,
                         RoundRepository.LoginRegisterCallback callback) {
        loginOrRegister(playerName, password, true, callback);
    }

    /**
     * Coge la respuesta del servidor y la convierte en una lista de partidas disponibles
     * @param response Respuesta del servidor
     * @param tipo Tipo de partidas que queremos filtrar
     * @return Lista de partidas
     */
    private List<Round> roundsFromJSONArray(JSONArray response, Type tipo, boolean player) {
        // Creamos la lista de partidas donde las iremos almacenando
        List<Round> rounds = new ArrayList<>();
        // Por cada uno de los objetos que tenemos recorremos e el iterador
        for (int i = 0; i < response.length(); i++) {
            try {
                // Cogemos el siguiente objeto JSON devuelto por el servidor
                JSONObject o = response.getJSONObject(i);

                // Cogemos cada uno de sus componentes
                // TODO ver que ostias se hace con el turn
                int roundid = o.getInt(ServerInterface.ROUND_ID_TAG);
                int numberofplayers = o.getInt(ServerInterface.ROUND_PLAYER_NUMBER_TAG);
                String dateevent = o.getString(ServerInterface.ROUND_DATE_TAG);
                String playernames = o.getString(ServerInterface.ROUND_PLAYER_NAMES_TAG);
                int turn = o.getInt(ServerInterface.ROUND_TURN_TAG);
                String codedboard = o.getString(ServerInterface.ROUND_CODEBOARD_TAG);

                // Creamos la partida correspondiente con los datos propocionados
                Round round = new Round(roundid, tipo, dateevent, Preferences.BOARD_SIZE_DEFAULT);
                round.getBoard().stringToTablero(codedboard);

                // Dependiendo del número de jugadores, rellenamos los nombres
                switch (numberofplayers){
                    case 1:
                        round.setFirstUser(playernames,"");
                        break;
                    case 2:
                        StringTokenizer stok = new StringTokenizer(playernames, ",");
                        round.setFirstUser(stok.nextToken(),"");
                        round.setSecondUser(stok.nextToken(),"");
                        break;
                }


                if (((player) && (playernames.contains(Preferences.getPlayerName(this.context)))) ||
                        ((!player) && (!playernames.contains(Preferences.getPlayerName(this.context)))))
                    rounds.add(round);

            } catch (JSONException e) {
                e.printStackTrace();
            } catch (ExcepcionJuego excepcionJuego) {
                excepcionJuego.printStackTrace();
                Log.d(DEBUG, "Error turning string into Tablero");
            }
        }
        return rounds;
    }

    /**
     * Devuelve una lista de partidas obtenida desde el servidor
     * @param playeruuid Identificador de usuario
     * @param orderByField Orden en el que se devolverán las partidas
     * @param filter Filtra por el tipo de partida que se ha de buscar en el servidor
     * @param callback Callback a ejecutar al que se le notificará cómo funcionó la función
     */
    @Override
    public void getRounds(String playeruuid, String orderByField, Round.Type filter,
                          final RoundsCallback callback) {
        if (filter == null) filter = OPEN;

        // Comprobamos que tipo de partida nos están pidiendo y solo buscamos esas en el servidor
        switch (filter){
            case LOCAL:
                break;
            case OPEN:
                this.getOpenRounds(playeruuid,callback);
                break;
            case ACTIVE:
                this.getActiveRounds(playeruuid,callback);
                this.getMyOpenRounds(playeruuid,callback);
                break;
            case FINISHED:
                this.getFinishedRounds(playeruuid,callback);
                break;
        }
    }

    /**
     * Identifica el filtro por defecto que se utilizará para recuperar las partidas
     * @return Tipo por defecto del friltro para seleccionar partidas
     */
    @Override
    public Type getDefaultFilter() {
        return Round.Type.ACTIVE;
    }

    /**
     * Devuelve la lista de partidas abiertas a las que el usuario se puede añadir
     * @param playeruuid Identificador del usuario
     * @param callback Calback a ejecutar con la respuesta del servidor
     */
    public void getOpenRounds(String playeruuid, final RoundsCallback callback) {
        // Registramos un listener para manejar el correcto funcionamiento de la petición en el servidor
        Response.Listener<JSONArray> responseCallback = new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                // Obtenemos la lista de partidas y se las mandamos al callbacki
                List<Round> rounds = roundsFromJSONArray(response, OPEN, false);
                callback.onResponse(rounds);
                Log.d(DEBUG, "Rounds downloaded from server");
            }
        };

        // Registramos un listener para manejar el mal funcionamiento de la petición en el servidor
        Response.ErrorListener errorCallback = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Enviamos un error al callback
                callback.onError("Error downloading rounds from server");
                Log.d(DEBUG, "Error downloading rounds from server");
            }
        };

        // Obtenemos la lista de partidas de la interfaz con el servidor
        is.getOpenRounds(playeruuid, responseCallback, errorCallback);
    }

    /**
     * Devuelve la lista de partidas abiertas que el usuario tiene para que se unan
     * @param playeruuid Identificador del usuario
     * @param callback Calback a ejecutar con la respuesta del servidor
     */
    public void getMyOpenRounds(String playeruuid, final RoundsCallback callback) {
        // Registramos un listener para manejar el correcto funcionamiento de la petición en el servidor
        Response.Listener<JSONArray> responseCallback = new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                // Obtenemos la lista de partidas y se las mandamos al callbacki
                List<Round> rounds = roundsFromJSONArray(response, OPEN, true);
                callback.onResponse(rounds);
                Log.d(DEBUG, "Rounds downloaded from server");
            }
        };

        // Registramos un listener para manejar el mal funcionamiento de la petición en el servidor
        Response.ErrorListener errorCallback = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Enviamos un error al callback
                callback.onError("Error downloading rounds from server");
                Log.d(DEBUG, "Error downloading rounds from server");
            }
        };

        // Obtenemos la lista de partidas de la interfaz con el servidor
        is.getOpenRounds(playeruuid, responseCallback, errorCallback);
    }

    /**
     * Devuelve la lista de partidas disponibles con las que el usuario puede jugar
     * @param playeruuid Identificador del usuario
     * @param callback Calback a ejecutar con la respuesta del servidor
     */
    public void getActiveRounds(String playeruuid, final RoundsCallback callback) {
        // Registramos un listener para manejar el correcto funcionamiento de la petición en el servidor
        Response.Listener<JSONArray> responseCallback = new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                // Obtenemos la lista de partidas y se las mandamos al callbacki
                List<Round> rounds = roundsFromJSONArray(response, ACTIVE, true);
                callback.onResponse(rounds);
                Log.d(DEBUG, "Rounds downloaded from server");
            }
        };

        // Registramos un listener para manejar el mal funcionamiento de la petición en el servidor
        Response.ErrorListener errorCallback = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Enviamos un error al callback
                callback.onError("Error downloading rounds from server");
                Log.d(DEBUG, "Error downloading rounds from server");
            }
        };

        // Obtenemos la lista de partidas de la interfaz con el servidor
        is.getActiveRounds(playeruuid, responseCallback, errorCallback);
    }

    /**
     * Devuelve la lista de partidas finalizadas
     * @param playeruuid Identificador del usuario
     * @param callback Calback a ejecutar con la respuesta del servidor
     */
    public void getFinishedRounds(String playeruuid, final RoundsCallback callback) {
        // Registramos un listener para manejar el correcto funcionamiento de la petición en el servidor
        Response.Listener<JSONArray> responseCallback = new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                // Obtenemos la lista de partidas y se las mandamos al callbacki
                List<Round> rounds = roundsFromJSONArray(response, FINISHED, true);
                callback.onResponse(rounds);
                Log.d(DEBUG, "Rounds downloaded from server");
            }
        };

        // Registramos un listener para manejar el mal funcionamiento de la petición en el servidor
        Response.ErrorListener errorCallback = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Enviamos un error al callback
                callback.onError("Error downloading rounds from server");
                Log.d(DEBUG, "Error downloading rounds from server");
            }
        };

        // Obtenemos la lista de partidas de la interfaz con el servidor
        is.getFinishedRounds(playeruuid, responseCallback, errorCallback);
    }

    /**
     * Añade una ronda al servidor
     * @param round Partida que queremos añadir
     * @param callback Callback a ejecutar con la respuesta a la función
     */
    @Override
    public void addRound(Round round, final BooleanCallback callback) {

        // Registramos un listener para manejar el correcto funcionamiento de la petición en el servidor
        Response.Listener<String> responseCallback = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //TODO comprobar si se ha creado bien la ronda
                // Enviamos un exito al callback
                callback.onResponse(true);
                Log.d(DEBUG, "Round created correctly");
            }
        };

        // Registramos un listener para manejar el mal funcionamiento de la petición en el servidor
        Response.ErrorListener errorCallback = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Enviamos un error al callback
                callback.onResponse(false);
                Log.d(DEBUG, "Error creating round");
            }
        };

        // Obtenemos la lista de partidas de la interfaz con el servidor
        is.newRound(round.getFirstUserUUID()
                , round.getBoard().tableroToString(), responseCallback, errorCallback);
    }

    /**
     * Añade un nuevo jugador a una partida
     * @param round Ronda a la que añadiremos el jugador
     * @param userUUID UUID del jugador que queremos añadir
     * @param callback Callbacka ejecutar con la respuesta al función
     */
    public void addPlayerToRound(Round round, String userUUID, final BooleanCallback callback) {
        // Registramos un listener para manejar el correcto funcionamiento de la petición en el servidor
        Response.Listener<String> responseCallback = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //TODO comprobar si se ha creado bien la ronda
                // Enviamos un exito al callback
                callback.onResponse(true);
                Log.d(DEBUG, "Round created correctly");
            }
        };

        // Registramos un listener para manejar el mal funcionamiento de la petición en el servidor
        Response.ErrorListener errorCallback = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Enviamos un error al callback
                callback.onResponse(false);
                Log.d(DEBUG, "Error creating round");
            }
        };
        is.addPlayerToRound(Integer.parseInt(round.getId()),userUUID,responseCallback,errorCallback);
    }

    @Override
    public void updateRound(Round round, final BooleanCallback callback) {
        // Registramos un listener para manejar el correcto funcionamiento de la petición en el servidor
        Response.Listener<String> responseCallback = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //TODO comprobar si se ha creado bien la ronda
                // Enviamos un exito al callback
                callback.onResponse(true);
                Log.d(DEBUG, "Round updated correctly");
            }
        };

        // Registramos un listener para manejar el mal funcionamiento de la petición en el servidor
        Response.ErrorListener errorCallback = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Enviamos un error al callback
                callback.onResponse(false);
                Log.d(DEBUG, "Error updating round");
            }
        };
        is.newMovement(Integer.parseInt(round.getId()), Preferences.getPlayerUUID(context),
                round.getBoard().tableroToString(),responseCallback,errorCallback);
    }
}
