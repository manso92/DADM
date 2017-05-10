package es.uam.eps.dadm.events;

import es.uam.eps.dadm.model.Round;

/**
 * @author Pablo Manso
 * @version 10/05/2017
 */

public class RefreshRoundEvent {

    /**
     * Tipo de la ronda que deberemos actualizar
     */
    private Round.Type tipo;

    /**
     * Constructor del evento en el que le indicamos el tipo de rounds que hay que recargar
     * @param tipo Tipo de partida que hay que recargar
     */
    public RefreshRoundEvent(Round.Type tipo) {
        this.tipo = tipo;
    }

    // GETTERS Y SETTERS
    public Round.Type getTipo() { return tipo; }
    public void setTipo(Round.Type tipo) { this.tipo = tipo; }
}
