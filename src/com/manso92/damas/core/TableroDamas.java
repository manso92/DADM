package com.manso92.damas.core;

import es.uam.eps.multij.*;
import java.util.ArrayList;
import java.util.StringTokenizer;

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
        this.limpiaTablero();
        this.numJugadas=0;
        this.numJugadores=2;
        this.estado=EN_CURSO;
        movimientosValidos = this.movimientosValidos();
    }

    /**
     * Crea un tablero vacío
     */
    public void limpiaTablero(){
        // Limpia el tablero poniendo todo a cero
        for(int i=0; i<this.casillas.length; i++)
            for(int j=0; j<this.casillas[i].length; j++)
                this.casillas[i][j] = new Casilla(i,j);
    }

    /**
     * Coloca en el tablero las fichas en la posición original
     */
    public void colocaFichas(){
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
     * Ejecuta un movimiento en el tablero
     * @param m Movimiento a realizar
     * @throws ExcepcionJuego Se lanzará esta excepción en caso de no poder ejecutar el movmiento
     */
    @Override
    protected void mueve(Movimiento m) throws ExcepcionJuego {
        if (!this.esValido(m))
            throw new ExcepcionJuego("No se puede realizar el movimiento que quieres;");

        this.ejecutaMovimiento(m);
        this.numJugadas++;
        this.cambiaTurno();
        this.movimientosValidos = this.movimientosValidos();
    }

    /**
     * Comprueba si un movimiento es válido mirando si está en la lista de movimientos
     * @param m Movimiento a comprobar si es válido o no
     * @return Nos indica la validez o no de un movimiento
     */
    @Override
    public boolean esValido(Movimiento m) {
        return movimientosValidos.indexOf(m) != -1;
    }

    @Override
    public ArrayList<Movimiento> movimientosValidos() {
        ArrayList<Movimiento> movmientos = new ArrayList<Movimiento>();
        Ficha.Color color = this.getTurno() == 0 ? Ficha.Color.BLANCA : Ficha.Color.NEGRA ;
        for(int i=0 ; i<this.casillas.length ; i++)
            for(int j=0 ; j<this.casillas[i].length ; j++)
                if (this.casillas[i][j].tieneFicha() &&
                    this.casillas[i][j].getFicha().color == color)
                    this.movimientosValidos(this.casillas[i][j], movmientos);
        return movmientos;
    }

    /**
     * Exporta una partida para que se pueda jugar en otro momento. Ésta será cargada por
     * el método {@link TableroDamas#stringToTablero(String)}
     * @return String que contiene la partida
     */
    @Override
    public String tableroToString() {
        String ret = "" + this.getNumJugadas() + "/";
        // Para cada celda del tablero
        for(int i=0;i<TABLEROSIZE;i++)
            for (int j = 0; j < TABLEROSIZE; j++)
            // Si no tiene fichas no conviene exportarlo
                if (this.casillas[i][j].tieneFicha()){
                    // Añadimos el color a la salida
                    if (this.casillas[i][j].getFicha().color == Ficha.Color.BLANCA) ret += "0";
                    if (this.casillas[i][j].getFicha().color == Ficha.Color.NEGRA)  ret += "1";

                    // Añadimos el tipo de ficha que es
                    if (this.casillas[i][j].getFicha().getTipo() == Ficha.Tipo.DAMA)  ret += "0";
                    if (this.casillas[i][j].getFicha().getTipo() == Ficha.Tipo.REINA) ret += "1";

                    // Añadimos la posición de la ficha
                    ret += i + "" + j;
                }

        return ret;
    }

    /**
     * Carga una partida anterior para continuar jugándola
     * @param cadena Descripción de la partida generada por el método {@link TableroDamas#tableroToString()}
     * @throws ExcepcionJuego Se lanzará esta excepción si el tablero pasado no cumple con lo establecido
     */
    @Override
    public void stringToTablero(String cadena) throws ExcepcionJuego {
        // Partimos la cadena por el separador de los parámetros
        StringTokenizer stok = new StringTokenizer(cadena, "/");

        // Si no hay exáctamente dos parámetros hay algo mal
        if (stok.countTokens() != 2)
            throw new ExcepcionJuego("String no válido para un TableroDamas");

        // Copiamos los dos parámetros
        int jugadas = Integer.parseInt(stok.nextToken());
        String tablero = stok.nextToken();

        // Creamos un tablero vacío
        Casilla[][] casillas = new Casilla[TABLEROSIZE][TABLEROSIZE];
        for (int i = 0; i < TABLEROSIZE; i++)
            for (int j = 0; j < TABLEROSIZE; j++)
                casillas[i][j] = new Casilla(i,j);

        // Cada ficha ocupa cuatro caracteres, si no es múltiplo de 4 es que hay un error
        if ((tablero.length() % 4) != 0)
            throw new ExcepcionJuego("String no válido para un TableroDamas");



        for (int i = 0; i < tablero.length(); i+=4) {
            // Por cada 4, cogemos los parámetros y comprobamos que estén entre los valores adecuados
            int color = Character.getNumericValue(tablero.charAt(i));
            int tipo = Character.getNumericValue(tablero.charAt(i+1));
            int fila = Character.getNumericValue(tablero.charAt(i+2));
            int columna = Character.getNumericValue(tablero.charAt(i+3));
            if (color != 0 && color != 1) throw new ExcepcionJuego("String no válido para un TableroDamas");
            if (tipo != 0 && tipo != 1)   throw new ExcepcionJuego("String no válido para un TableroDamas");
            if (fila < 0 || fila >= TABLEROSIZE)       throw new ExcepcionJuego("String no válido para un TableroDamas");
            if (columna < 0 || columna >= TABLEROSIZE) throw new ExcepcionJuego("String no válido para un TableroDamas");

            // Creamos la ficha y la ponemos en el tablero
            Ficha.Color colorFicha = (color == 0) ? Ficha.Color.BLANCA : Ficha.Color.NEGRA;
            casillas[fila][columna].ponFicha(new Ficha(colorFicha));
            if (tipo == 1) casillas[fila][columna].getFicha().reina();
        }

        // Ajustamos los parámetros de partida y cargamos el tablero
        this.numJugadas = jugadas;
        if ((this.numJugadas % 2) == 1) this.turno = 1; else this.turno = 0;
        this.casillas = casillas;
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
