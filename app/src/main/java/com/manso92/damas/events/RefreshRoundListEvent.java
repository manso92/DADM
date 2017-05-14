package com.manso92.damas.events;

import com.manso92.damas.model.Round;

/**
 * @author Pablo Manso
 * @version 10/05/2017
 */

public class RefreshRoundListEvent {

    /**
     * Tag para escribir en el log
     */
    public static final String DEBUG = "Damas.Event.RRound";

    /**
     * Tipo de la ronda que deberemos actualizar
     */
    private Round.Type tipo;

    /**
     * Constructor del evento en el que le indicamos el tipo de rounds que hay que recargar
     * @param tipo Tipo de partida que hay que recargar
     */
    public RefreshRoundListEvent(Round.Type tipo) {
        this.tipo = tipo;
    }

    // GETTERS Y SETTERS
    public Round.Type getTipo() { return tipo; }
    public void setTipo(Round.Type tipo) { this.tipo = tipo; }
}
