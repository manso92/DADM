package com.manso92.damas.core;

import es.uam.eps.multij.Jugador;

/**
 * Ficha de las damas, que puede ser una dama o una reina
 * @author Pablo Manso
 * @version 11/02/2017
 */
public class Ficha {

    /**
     * Tipos de fichas
     */
    public enum Tipo { DAMA, REINA }

    /**
     * Color de la ficha
     */
    public enum Color { BLANCA, NEGRA}

    /**
     * Tipo de la ficha
     */
    private Tipo tipo;

    /**
     * Color de la ficha
     */
    private Color color;

    /**
     * Crea una nueva ficha en el tablero
     * @param color Color de la ficha con el que será pintado en el tablero
     */
    public Ficha (Color color){
        this.tipo = Tipo.DAMA;
        this.color = color;
    }

    /**
     * Devuelve el Tipo de la ficha, Reina o Dama
     * @return tipo de la ficha
     */
    public Tipo getTipo() { return tipo; }

    /**
     * Cambia el tipo de una ficha de Dama a Reina
     */
    public void reina() { this.tipo = Tipo.REINA; }

    /**
     * Escapa un string para que se pinte la ficha con el color que le corresponde
     * @return Cadena que representa la ficha
     */
    public String toString() {
        return (char)27 + "[1;" + (this.color == Color.BLANCA ? "30" : "31") + "m" + (this.tipo == Tipo.REINA ? "R" : "D");
    }

}
