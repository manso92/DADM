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
     * Constructor de Casilla en el que le indicamos la posición de la casilla
     * @param row Fila de la casilla en el tablero
     * @param col Columna de la casilla en el tablero
     */
    public Casilla(int row, int col) {
        this.row = row;
        this.col = col;
    }

    /**
     * Nos devuelve un caracter para pintar la casilla en ASCII
     * @return "X" para casillas negras , " " para casillas blancas
     */
    public String getColorCaracter (){
        return (row + col)%2==0 ? " " : "X";
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
     * Nos devuelve el identificador de la casilla. De este modo la casilla en la posición (0,0) será devuelta como "A1"
     * @return Cadena con el identificador
     */
    public String toString(){ return Character.toString((char) (this.getRow()+'A')) + Character.toString((char) (this.getCol()+'1')); }
}
