package es.uam.eps.dadm.model;

import android.content.Context;

import android.util.Log;

import es.uam.eps.dadm.R;
import es.uam.eps.dadm.events.ShowMsgEvent;
import es.uam.eps.dadm.view.activities.Jarvis;
import es.uam.eps.dadm.view.views.TableroView;
import es.uam.eps.multij.*;

/**
 * Jugador que moverá según lo que hagamos en la pantalla
 * @author Pablo Manso
 * @version 12/02/2017
 */
public class JugadorHumano implements Jugador, TableroView.OnPlayListener{

    /**
     * Tag para escribir en el log
     */
    public static final String DEBUG = "Damas.JugHumano";

    /**
     * Partida que está jugando este jugador
     */
    private Partida game;

    /**
     * Nombre que identifica al jugador
     */
    private String nombre = "Local player";

    /**
     * Repositorio de datos donde iremos actualizando la partida
     */
    private RoundRepository repository;

    /**
     * Ronda que se está jugando
     */
    private Round round;

    /**
     * Construye un jugador humano con el nombre por defecto
     */
    public JugadorHumano(Context context, Round round) {
        this.repository = RoundRepositoryFactory.createRepository(context,false);
        this.round = round;
    }

    /**
     * Construye un jugador humano
     * @param nombre Nombre del jugador
     */
    public JugadorHumano(String nombre, Round round, Context context) {
        this(context, round);
        this.nombre = nombre;
    }

    /**
     * Gestiona las respuestas del jugador a determinados eventos del juego
     * @param evento Evento hasta el que tenemos que generar una respuesta
     */
    @Override
    public void onCambioEnPartida(Evento evento) {
        switch (evento.getTipo()) {
            case Evento.EVENTO_CAMBIO:
                break;
            case Evento.EVENTO_CONFIRMA:
                break;
            case Evento.EVENTO_TURNO:
                break;
        }
    }

    /**
     * Devuelve 'true' si este jugador sabe jugar al juego indicado
     * @param tablero Tablero de la partida
     * @return true o false, dependiendo de si puede jugar o no
     */
    @Override
    public boolean puedeJugar(Tablero tablero) {
        return tablero instanceof TableroDamas;
    }

    /**
     * Asigna al jugador una partida
     * @param game Parida que va a jugar el jugador
     */
    public void setPartida(Partida game) {
        this.game = game;
    }

    /**
     * Nombre del jugador
     * @return string con el nombre del jugador
     */
    @Override
    public String getNombre() { return this.nombre; }

    /**
     * Función que implementa la interfaz ERView.OnPlayListener
     * De este modo la interfaz gráfica indica a la clase cual es el movimiento que el usuario ha
     * introducido por la pantalla para que este se lo comunique a la lógica del juego
     * @param movimientoDamas El movimiento que el usuario ha indicado
     */
    @Override
    public void onPlay(MovimientoDamas movimientoDamas) {
        try {
            // Si no estamos jugando no hacemos nada
            if (this.game.getTablero().getEstado() != Tablero.EN_CURSO)
                return;

            // Indicamos al juego que el jugador quiere hacer el movimiento
            this.game.realizaAccion(new AccionMover(this, movimientoDamas));
            this.updateRound();
        } catch (Exception e) {}
    }

    public void updateRound(){

        RoundRepository.BooleanCallback callback = new RoundRepository.BooleanCallback() {
            /**
             * Gestiona la respuesta del servidor a un evento de respuesta booelana
             * @param response Correcta ejecución de la función en el servidor
             */
            @Override
            public void onResponse(boolean response) {
                // Si se produce un error al actualizar la partida, se lo comunicamos al usuario
                if (!response)
                    Jarvis.error(ShowMsgEvent.Type.TOAST, "Error updating round");
            }
        };
        // Actualizamos la partida en la base de datos
        repository.updateRound(round, callback);
    }
}
