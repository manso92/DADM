package com.manso92.damas.core;

import es.uam.eps.multij.*;
import java.util.ArrayList;

/**
 * Tablero describe el tablero de las damas y evalúa los movimientos que se pueden o no hacer, las jugadas,
 * las fichas y todo lo relativo al teclado
 * @author Pablo Manso
 * @version 10/02/2017
 */
public class TableroDamas extends Tablero {

    /**
     * Tamaño que tiene el tablero
     */
    public final static int TABLEROSIZE = 8;

    /**
     * Doble array que contendrá toda la info del tablero
     */
    Casilla[][] casillas;

    /**
     * Registra una lista con los movimientos validos en cada turno
     */
    ArrayList<Movimiento> movimientosValidos = null;

    /**
     * Construye el tablero para jugar a las damas
     */
    public TableroDamas() {
        super();
        this.casillas = new Casilla[TABLEROSIZE][TABLEROSIZE];
        this.numJugadas=0;
        this.numJugadores=2;
        this.estado=EN_CURSO;
        this.colocaFichas();
        movimientosValidos = this.movimientosValidos();
    }

    /**
     * Inicializa el array que describe el tablero a una partida nueva
     */
    public void colocaFichas(){
        // Limpia el tablero poniendo todo a cero
        for(int i=0; i<this.casillas.length; i++)
            for(int j=0; j<this.casillas[i].length; j++)
                this.casillas[i][j] = new Casilla(i,j);


        // Colocamos las fichas del primer jugador en la parte superior del tablero
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < TABLEROSIZE; j++)
                if ((i + j) % 2 == 1)
                    this.casillas[i][j].ponFicha(new Ficha(Ficha.Color.BLANCA));


        // Colocamos las fichas del segundo jugador en la parte inferior del tablero
        for (int i = TABLEROSIZE - 3; i < TABLEROSIZE; i++)
            for (int j = 0; j < TABLEROSIZE; j++)
                if ((i + j) % 2 == 1)
                    this.casillas[i][j].ponFicha(new Ficha(Ficha.Color.NEGRA));
    }


    /**
     * Convierte el tablero en algo que un humano pueda entender para poder jugar la partida de forma gráfica
     * @return String con la partida a pintar
     */
    @Override
    public String toString() {
        String tablero = "";

        // Colocamos el nombre de las columnas
        tablero += " -12345678-\n";
        // Por cada fila
        for(int i=0; i<this.casillas.length; i++) {
            // Pintamos el nombre de la fila
            tablero += Character.toString((char) (i+'A')) + " ";
            // Imprimimos cada una de las casillas
            for(int j=0; j<this.casillas[i].length; j++)
                tablero += this.casillas[i][j].toString();
            tablero += "\n";
        }
        return tablero + "\n";
    }



    /**
     * Limpia el tablero de la partida actual y coloca las fichas para una partida nueva
     * @return true
     */
    @Override
    public boolean reset(){
        this.numJugadas = 0;
        this.colocaFichas();
        return true;
    }

    /**
     * Devuelve el contenido de una casilla del tablero
     * @param x Posición x del tablero
     * @param y Posición y del tablero
     * @return Contenido de la casilla
     */
    public Casilla getCasilla(int x, int y) {
        return this.casillas[x][y];
    }

    /**
     * Devuelve el contenido de una casilla del tablero
     * @param casilla Casilla de la que mirar el contenido
     * @return Contenido de la casilla
     */
    public Casilla getCasilla(Casilla casilla) {
        return this.casillas[casilla.getRow()][casilla.getCol()];
    }
}
