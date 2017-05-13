package es.uam.eps.dadm.server;


import android.content.Context;
import android.util.Log;
import android.view.View;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONObject;

import es.uam.eps.dadm.R;
import es.uam.eps.dadm.events.ShowMsgEvent;
import es.uam.eps.dadm.model.MovimientoDamas;
import es.uam.eps.dadm.model.Preferences;
import es.uam.eps.dadm.model.Round;
import es.uam.eps.dadm.model.RoundRepository;
import es.uam.eps.dadm.model.RoundRepositoryFactory;
import es.uam.eps.dadm.view.activities.Jarvis;
import es.uam.eps.dadm.view.views.TableroView;
import es.uam.eps.multij.*;

/**
 * Jugador que gestionará los toques que hacemos en pantalla y se los indicará al servidor
 *
 * @author Pablo Manso
 * @version 09/05/2017
 */
public class LocalServerPlayer  implements Jugador, TableroView.OnPlayListener {

    /**
     * Tag para escribir en el log
     */
    public static final String DEBUG = "Damas.LocSerPlayer";

    /**
     * Ronda que estamos jugando
     */
    private Round round;

    /**
     * Context desde el que nos ejecutan para poder mostrar errores
     */
    private Context context;

    /**
     * Repositorio de datos en el que actualizar las partidas
     */
    private RoundRepository repository;

    /**
     * Partida que estamos jugando
     */
    private Partida game;

    /**
     * Nombre del jugador
     */
    private String nombre;

    /**
     * Constructor del jugador de la partida
     * @param context Context para mostrar los mensajes
     * @param round Ronda que se va a jugar
     */
    public LocalServerPlayer(Context context, Round round) {
        this.context = context;
        this.round = round;
        this.repository = RoundRepositoryFactory.createRepository(context,true);
    }

    /**
     * Nos indica si el tablero está sincronizado con el que se nos provee
     * @param codedboard String que representa el tablero
     * @return Si el tablero está actualizado o no
     */
    private boolean isBoardUpToDate(String codedboard) {
        return this.game.getTablero().tableroToString().equals(codedboard);
    }

    /**
     * Actualiza el tablero que se está jugando
     * @param codedboard String que representa el tablero
     * @throws ExcepcionJuego Se lanzará la excepción si por casualidad la cadena no está bien formateada
     */
    private void updateBoard (String codedboard) throws ExcepcionJuego {
        if (!isBoardUpToDate(codedboard))
            this.game.getTablero().stringToTablero(codedboard);
    }

    /**
     * Ejecuta un movimiento en el tablero por parte de nosotros
     * @param movimientoDamas Movimiento que tenemos que ejecutar
     */
    @Override
    public void onPlay(final MovimientoDamas movimientoDamas) {
        // Registramos un listener para manejar el correcto funcionamiento de la petición en el servidor
        Response.Listener<JSONObject> responseListener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    // Cogemos el turno y el tablero de la respuesta
                    int turn = response.getInt(ServerInterface.ROUND_TURN_TAG);
                    String codedboard = response.getString(ServerInterface.ROUND_CODEBOARD_TAG);

                    // Si no tenemos actualizado el tablero, lo actualizamos
                    updateBoard(codedboard);

                    // Si es nuestro turno
                    if (turn == 1){
                        try {
                            // Si no estamos jugando no hacemos nada
                            if (round.getBoard().getEstado() != Tablero.EN_CURSO)
                                return;

                            // Indicamos al juego que el jugador quiere hacer el movimiento
                            game.realizaAccion(new AccionMover(LocalServerPlayer.this, movimientoDamas));

                            // Enviamos al servidor nuestro movimiento
                            updateRound();

                        } catch (Exception e) {}
                    } else {
                        // Si no es nuestro turno, lo indicamos
                        Jarvis.error(ShowMsgEvent.Type.TOAST, R.string.game_not_turn, context);
                    }
                } catch (Exception e) {
                    Log.d(DEBUG, "" + e);
                }
            }
        };
        // Registramos un listener para manejar el mal funcionamiento de la petición en el servidor
        Response.ErrorListener errorListener = new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO MANEJAR EL ERROR
            }
        };

        // Preguntamos al servidor si es nuestro turno
        ServerInterface is = ServerInterface.getServer(context);
        is.isMyTurn(Integer.parseInt(round.getId()), Preferences.getPlayerUUID(context),
                responseListener, errorListener);
    }

    /**
     * Nombre del jugador
     * @return String que contiene el nombre del jugador
     */
    @Override
    public String getNombre() {
        return this.nombre;
    }

    @Override
    public boolean puedeJugar(Tablero tablero) {return false;}

    /**
     * Añade la partida al jugador
     * @param game Partida de juego
     */
    public void setPartida(Partida game){
        this.game = game;
    }

    @Override
    public void onCambioEnPartida(Evento evento) {}

    /**
     * Actualiza la ronda en el servidor
     */
    public void updateRound(){
        RoundRepository.BooleanCallback callback = new RoundRepository.BooleanCallback() {
            @Override
            public void onResponse(boolean response) {
                // Si se produce un error al actualizar la partida, se lo comunicamos al usuario
                if (!response)
                    Jarvis.error(ShowMsgEvent.Type.TOAST, R.string.repository_round_update_error, context);
            }
        };
        // Actualizamos la partida en la base de datos
        repository.updateRound(round, callback);
    }
}