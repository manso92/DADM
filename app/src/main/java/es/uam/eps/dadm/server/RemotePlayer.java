package es.uam.eps.dadm.server;

import es.uam.eps.multij.Evento;
import es.uam.eps.multij.Jugador;
import es.uam.eps.multij.Tablero;

/**
 * Clase que se usará para simbolizar al jugador online que juega contra nosotros pero que no hará nada
 *
 * @author Pablo Manso
 * @version 12/04/2017
 */
public class RemotePlayer implements Jugador{

    /**
     * Tag para escribir en el log
     */
    public static final String DEBUG = "Damas.RemPlayer";

    /**
     * Nombre del jugador para mostrar si fuera necesario
     */
    private String name = "Remote player";

    /**
     * Constructor vacío en el que coge un nombre por defecto
     */
    public RemotePlayer(){}

    /**
     * Constructor en el que se indica el nombre del jugador
     * @param name Nombre del jugador
     */
    public RemotePlayer(String name){
        this.name = name;
    }

    @Override
    public String getNombre() {
        return this.name;
    }
    @Override
    public boolean puedeJugar(Tablero tablero) {
        return false;
    }
    @Override
    public void onCambioEnPartida(Evento evento) {}
}
