package com.manso92.damas.core;

/**
 * Casilla es la unidad básica para cualquier juego de tablero en cuadrícula. El tablero para el que está implementada
 * esta clase es un tablero del tipo Ajedrez/Damas en el cual son cuadrículas con casillas blancas y y negragas.
 * @author Pablo Manso
 * @version 10/02/2017
 */
public class Casilla {

    /**
     * Representación de la fila del tablero
     */
    private int row;
    
    /**
     * Representación de la columna del tablero
     */
    private int col;

    /**
     * Ficha que está en la casilla
     */
    private Ficha ficha;

    /**
     * Constructor de Casilla en el que le indicamos la posición de la casilla
     * @param row Fila de la casilla en el tablero
     * @param col Columna de la casilla en el tablero
     */
    public Casilla(int row, int col) {
        this.row = row;
        this.col = col;
        this.ficha = null;
    }

    /**
     * Constructor de Casilla en el que le indicamos la posición de la casilla y colocamos una ficha
     * @param row Fila de la casilla en el tablero
     * @param col Columna de la casilla en el tablero
     * @param ficha Ficha que está en la casilla
     */
    public Casilla(int row, int col, Ficha ficha) {
        this.row = row;
        this.col = col;
        this.ficha = ficha;
    }

    /**
     * Nos devuelve la fila en la que se encuentra la casilla
     * @return Posición de la fila
     */
    public int getRow() {
        return row;
    }

    /**
     * Cambia la fila en la que se encuentra la casilla
     * @param row Posición de la fila
     */
    public void setRow(int row) {
        this.row = row;
    }

    /**
     * Nos devuelve la columna en la que se encuentra la casilla
     * @return Posición de la columna
     */
    public int getCol() {
        return col;
    }

    /**
     * Cambia la columna en la que se encuentra la casilla
     * @param col Posición de la columna
     */
    public void setCol(int col) {
        this.col = col;
    }

    /**
     * Nos indica si esta casilla tiene una ficha en ella
     * @return Si hay una ficha o no
     */
    public boolean tieneFicha(){ return this.ficha != null; }

    /**
     * Nos devuelve la ficha que hay en la casilla
     * @return La ficha de la casilla
     */
    public Ficha getFicha(){ return this.ficha; }

    /**
     * Coloca una ficha en esta casilla
     * @param ficha Ficha a colocar
     */
    public void ponFicha (Ficha ficha) {
        this.ficha = ficha;
    }

    /**
     * Quita la ficha de la casilla
     * @return La ficha que acabamos de quitar
     */
    public Ficha quitaFicha(){
        Ficha ficha = this.ficha;
        this.ficha = null;
        return ficha;
    }

    /**
     * Nos devuelve el identificador de la casilla. De este modo la casilla en la posición (0,0) será devuelta como "A1"
     * @return Cadena con el identificador
     */
    public String toString(){
        return (char)27 + "[" +  ((row + col)%2==0 ? "40" : "47") + "m" +
            (this.ficha == null ? " " : this.getFicha().toString());
    }

}
