package es.uam.eps.dadm.model;

/**
 * Casilla es la unidad básica para cualquier juego de tablero en cuadrícula. El tablero para el que está implementada
 * esta clase es un tablero del tipo Ajedrez/Damas en el cual son cuadrículas con casillas blancas y y negragas.
 * @author Pablo Manso
 * @version 12/02/2017
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
     * Constructor de Casilla en el que le indicamos la posición de la casilla y colocamos una ficha
     * @param origen Casilla de origen sobre la que calcular la nueva posición
     * @param row Filas a desplazar desde la casilla de origen
     * @param col Columnas a desplazar desde la casilla de destino
     */
    public Casilla(Casilla origen, int row, int col) {
        this.row = origen.row() + row;
        this.col = origen.col() + col;
        this.ficha = null;
    }

    /**
     * Constructor de Casilla en el que le indicamos la posición de la casilla y colocamos una ficha
     * @param origen Casilla de origen sobre la que calcular la nueva posición
     * @param row Filas a desplazar desde la casilla de origen
     * @param col Columnas a desplazar desde la casilla de destino
     * @param ficha Ficha que está en la casilla
     */
    public Casilla(Casilla origen, int row, int col, Ficha ficha) {
        this.row = origen.row() + row;
        this.col = origen.col() + col;
        this.ficha = ficha;
    }

    /**
     * Comprueba si la casilla que hemos creado está entre los límites del tablero
     * @return Si la casilla está o no en el tablero
     */
    public boolean enTablero(){
        return ((this.col() >= 0) && (this.col() < TableroDamas.TABLEROSIZE) &&
                (this.row() >= 0) && (this.row() < TableroDamas.TABLEROSIZE) );
    }

    /**
     * Nos devuelve la fila en la que se encuentra la casilla
     * @return Posición de la fila
     */
    public int row() {
        return row;
    }

    /**
     * Cambia la fila en la que se encuentra la casilla
     * @param row Posición de la fila
     */
    public void row(int row) {
        this.row = row;
    }

    /**
     * Nos devuelve la columna en la que se encuentra la casilla
     * @return Posición de la columna
     */
    public int col() {
        return col;
    }

    /**
     * Cambia la columna en la que se encuentra la casilla
     * @param col Posición de la columna
     */
    public void col(int col) {
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
     * Nos devuelve la reprresentación para pintar de la casilla
     * @return Cadena para pintar
     */
    public String string(){
    	if(TableroDamas.ECLIPSE)
    		return (this.ficha == null ? "  " : this.getFicha().toString());
		return "\033[" +  ((row + col)%2==0 ? "40" : "47") + "m" +
        	(this.ficha == null ? " " : this.getFicha().string()) + (char)27 + "[0m";
    }

    /**
     * Nos devuelve la posición de la casilla
     * @return Cadena con el identificador
     */
    @Override
    public String toString(){
        return Character.toString((char) (this.row() + 'A')) + Character.toString((char) (this.col() + '1'));
    }

    /**
     * Comprueba si dos casillas están ubicadas en la misma posición
     * @param o Casilla a comparar
     * @return Si son la misma casilla o no
     */
    @Override
    public boolean equals (Object o){
        return (this.row() == ((Casilla)o).row()) && this.col() == ((Casilla)o).col();
    }

    /**
     * Clona el objeto para que no haya errores de modificaciones de punteros
     * @return Objeto clonado
     */
    @Override
    public Casilla clone(){
        Casilla c = new Casilla(this.row(),this.col());
        if (this.tieneFicha()) c.ponFicha(this.getFicha().clone());
        return c;
    }

}
