package es.uam.eps.dadm.server;

import android.content.Context;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.StringTokenizer;

import es.uam.eps.dadm.model.Preferences;
import es.uam.eps.dadm.model.Round;
import es.uam.eps.dadm.model.RoundRepository;
import es.uam.eps.dadm.view.adapters.Message;
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
     * Tag para escribir en el log
     */
    public static final String DEBUG = "Damas.SRepository";

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
     * Interfaz que se deberá implementar cuando se solicitan mensajes
     */
    public interface MessagesCallback {
        void onResponse(List<Message> messages);
        void onError(String error);
    }

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
        is.login(playerName, password, Preferences.getFirebaseToken(context), login, response, error);
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

    /**
     * Actualiza una ronda en el servidor
     * @param round Partida que queremos actualizar
     * @param callback Callback a ejecutar con la respuesta a la función
     */
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

    /**
     * Envía un mensaje al chat de una ronda
     * @param from UUID de quien manda el mensaje
     * @param to Id de la ronda a la que se manda el mensaje
     * @param msg Mensaje que se quiere mandar
     * @param callback Callback que gestionará la respuesta
     */
    public void sendMessageToRound(String from, String to, String msg, BooleanCallback callback) {
        this.sendMessage(from, to, msg, true, callback);
    }

    /**
     * Envía un mensaje a un usuario en particular
     * @param from UUID de quien manda el mensaje
     * @param to Nombre de usuario al que queremos mandar el mensaje
     * @param msg Mensaje que se quiere mandar
     * @param callback Callback que gestionará la respuesta
     */
    public void sendMessageToUser(String from, String to, String msg, BooleanCallback callback) {
        this.sendMessage(from, to, msg, false, callback);
    }

    /**
     * Envía un mensaje al servidor, a una ronda o a un usuario
     * @param from UUID de quien manda el mensaje
     * @param to A quién se manda el mensaje
     * @param msg Mensaje que se quiere mandar
     * @param round Si el mensaje es para una ronda o si es para un usuario
     * @param callback Callback que gestionará la respuesta
     */
    private void sendMessage(String from, String to, String msg, boolean round, final BooleanCallback callback) {
        // Registramos un listener para manejar el correcto funcionamiento de la petición en el servidor
        Response.Listener<String> responseCallback = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                // Enviamos un exito al callback
                callback.onResponse(response.trim().equals("1"));
                Log.d(DEBUG, "SendMessage peticion response correctly");
            }
        };

        // Registramos un listener para manejar el mal funcionamiento de la petición en el servidor
        Response.ErrorListener errorCallback = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Enviamos un error al callback
                callback.onResponse(false);
                Log.d(DEBUG, "Error enviando el mensaje");
            }
        };
        // Enviamos el mensaje al servidor
        is.sendMessages(from, to, msg, round, responseCallback, errorCallback);
    }

    /**
     * Convierte un JSONArray en una lista de mensajes
     * @param response JSONArray que nos devuelve el servidor
     * @return Lista de mensajes obtenidos del servidor
     */
    private List<Message> messagesFromJSONArray(JSONArray response) {
        // Lista de mensajes que devolveremos
        List<Message> messages = new ArrayList<>();

        // Por cada uno de los objetos que tenemos recorremos e el iterador
        for (int i = 0; i < response.length(); i++) {
            try {
                // Obtenemos el mensaje y sus propiedades
                JSONObject o = response.getJSONObject(i);
                String from = o.getString("playername");
                String message = o.getString("message");
                String date = o.getString("msgdate");
                boolean self = from.equals(Preferences.getPlayerName(context));

                // Creamos un mensaje y lo añadimos a la lista
                Message msg = new Message(from, message, self, date);
                messages.add(msg);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return messages;
    }

    /**
     * Obtiene los mensajes que se han enviado a una ronda
     * @param id Id de la ronda del que se quieren obtener los mensajes
     * @param callback Callback que gestionará la respuesta
     */
    public void getRoundMessages(String id, final MessagesCallback callback) {
        // Registramos un listener para manejar el correcto funcionamiento de la petición en el servidor
        Response.Listener<JSONArray> responseCallback = new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                // Obtenemos la lista de mensajes que el servidor nos ha mandado
                List<Message> messages = messagesFromJSONArray(response);
                // Los ordenamos de forma que los más recientes sean los últimos
                Collections.sort(messages, new Comparator<Message>() {
                    @Override
                    public int compare(Message first, Message second) {
                        return first.getDate().compareTo(second.getDate());
                    }
                });

                // Enviamos los mensajes al callback
                callback.onResponse(messages);
                Log.d(DEBUG, "Mensajes correctamente recibidos");
            }
        };

        // Registramos un listener para manejar el mal funcionamiento de la petición en el servidor
        Response.ErrorListener errorCallback = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Enviamos un error al callback
                callback.onError("Error al obtener los mensajes del servidor.");
                Log.d(DEBUG, "Error al obtener los mensajes del servidor");
            }
        };

        // Obtiene los mensajes de la interfaz
        is.getMessages(id, true, responseCallback, errorCallback);
    }

    /**
     * Obtiene los mensajes que se han enviado con un usuario
     * @param user Username del usuario del que se quieren obtener los mensajes
     * @param callback Callback que gestionará la respuesta
     */
    public void getUserMessages(final String user, final MessagesCallback callback) {
        // Registramos un listener para manejar el correcto funcionamiento de la petición en el servidor
        Response.Listener<JSONArray> responseCallback = new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                // Obtenemos la lista de mensajes que el servidor nos ha mandado
                List<Message> messages = messagesFromJSONArray(response);

                messages = filterUser(user, messages);

                // Los ordenamos de forma que los más recientes sean los últimos
                Collections.sort(messages, new Comparator<Message>() {
                    @Override
                    public int compare(Message first, Message second) {
                        return first.getDate().compareTo(second.getDate());
                    }
                });

                // Enviamos los mensajes al callback
                callback.onResponse(messages);
                Log.d(DEBUG, "Mensajes correctamente recibidos");
            }
        };

        // Registramos un listener para manejar el mal funcionamiento de la petición en el servidor
        Response.ErrorListener errorCallback = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Enviamos un error al callback
                callback.onError("Error al obtener los mensajes del servidor.");
                Log.d(DEBUG, "Error al obtener los mensajes del servidor");
            }
        };

        // Obtiene los mensajes de la interfaz
        is.getMessages(Preferences.getPlayerUUID(context), true, responseCallback, errorCallback);
    }

    /**
     * Elimina todos los mensajes que no nos haya enviado un usuario
     * @param user Usuario a filtrar
     * @param messages Mensajes a filtrar
     * @return Mensajes filtrados
     */
    private List<Message> filterUser(String user, List<Message> messages){
        List <Message> returned = new ArrayList<>();
        for (Message m : messages)
            if (m.getFromName().equals(user))
                returned.add(m);
        return returned;
    }

    /**
     * Busca el último mensaje de cada usuario
     * @param callback Callback que gestionará la respuesta
     */
    public void getLastMessages(final MessagesCallback callback) {
        // Registramos un listener para manejar el correcto funcionamiento de la petición en el servidor
        Response.Listener<JSONArray> responseCallback = new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                // Obtenemos la lista de mensajes que el servidor nos ha mandado
                List<Message> messages = messagesFromJSONArray(response);
                // Los ordenamos de forma que los más recientes sean los últimos
                Collections.sort(messages, new Comparator<Message>() {
                    @Override
                    public int compare(Message first, Message second) {
                        return -1 * first.getDate().compareTo(second.getDate());
                    }
                });

                messages = usersFromMessages(messages);

                // Enviamos los mensajes al callback
                callback.onResponse(messages);
                Log.d(DEBUG, "Mensajes correctamente recibidos");
            }
        };

        // Registramos un listener para manejar el mal funcionamiento de la petición en el servidor
        Response.ErrorListener errorCallback = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Enviamos un error al callback
                callback.onError("Error al obtener los mensajes del servidor.");
                Log.d(DEBUG, "Error al obtener los mensajes del servidor");
            }
        };

        // Obtiene los mensajes de la interfaz
        is.getMessages(Preferences.getPlayerUUID(context), false, responseCallback, errorCallback);
    }

    /**
     * Devuelve el último mensaje de cada uno de los usuarios
     * @param messages Mensajes a filtrar
     * @return Mensajes filtrados
     */
    private List<Message> usersFromMessages(List<Message> messages){
        List<String> users = new ArrayList<>();
        List <Message> returned = new ArrayList<>();
        for (Message m : messages)
            if ((users.indexOf(m.getFromName()) == -1) &&
                    (!m.getFromName().equals(Preferences.getPlayerName(context)))) {
                users.add(m.getFromName());
                returned.add(m);
            }

        return returned;
    }
}
