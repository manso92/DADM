package es.uam.eps.dadm.server;

import android.content.Context;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import es.uam.eps.dadm.model.RoundRepository;

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
                    Log.d(DEBUG, "Logged in: " + result.trim());
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
}