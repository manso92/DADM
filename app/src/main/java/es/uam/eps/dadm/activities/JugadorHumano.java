package es.uam.eps.dadm.activities;

import es.uam.eps.dadm.model.MovimientoDamas;
import es.uam.eps.dadm.model.TableroDamas;
import es.uam.eps.dadm.views.ERView;
import es.uam.eps.multij.*;

/**
 * Jugador que moverá según lo que hagamos en la pantalla
 * @author Pablo Manso
 * @version 12/02/2017
 */
public class JugadorHumano implements Jugador, ERView.OnPlayListener{

    /**
     * Partida que está jugando este jugador
     */
    private Partida game;

    /**
     * Nombre que identifica al jugador
     */
    private String nombre = "Local player";

    /**
     * Construye un jugador humano con el nombre por defecto
     */
    public JugadorHumano() {}

    /**
     * Construye un jugador humano
     * @param string Nombre del jugador
     */
    public JugadorHumano(String string) {
        this.nombre=string;
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
        } catch (Exception e) {}
    }
}
