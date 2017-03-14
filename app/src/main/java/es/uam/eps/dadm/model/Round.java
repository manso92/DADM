package es.uam.eps.dadm.model;

import java.util.Date;
import java.util.UUID;

/**
 * Esta clase almacenará los datos de una partida de la aplicación
 *
 * @author Pablo Manso
 * @version 13/03/2017
 */
public class Round {
    /**
     * Indentificador de la partida
     */
    private String id;
    /**
     * Título de la partida
     */
    private String title;
    /**
     * Fecha de la partida
     */
    private String date;
    /**
     * Tablero asociado a la partida
     */
    private TableroDamas board;

    /**
     * Constructor para una partida
     */
    public Round() {
        // Generamos un nombre e identifiador de forma aleatoria
        id = UUID.randomUUID().toString();
        title = "ROUND " + id.toString().substring(19, 23).toUpperCase();
        // Ajustamos la fecha actual y creamos un nuevo tablero para la partida
        date = new Date().toString();
        board = new TableroDamas();
    }

    /**
     * Indentificador de la partida
     * @return identificador de la partida
     */
    public String getId() {
        return id;
    }

    /**
     * Cambia el identificador de la partida
     * @param id Nuevo indentificador de la partida
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Nos devuelve el título de l apartida
     * @return Título de la partida
     */
    public String getTitle() {
        return title;
    }

    /**
     * Cambia el título de la partida
     * @param title Nuevo título de la partida
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Devuelve la fecha en la que se modificó la partida
     * @return Fecha de modificación
     */
    public String getDate() {
        return date;
    }

    /**
     * Cambia la fecha en la que se modificó la partida
     * @param date Nueva fecha de la partida
     */
    public void setDate(String date) {
        this.date = date;
    }

    /**
     * Devuelve el tablero de la partida
     * @return Tablero de la partida
     */
    public TableroDamas getBoard() {
        return board;
    }

    /**
     * Cambia el tablero de la partida
     * @param board Nuevo tablero de la partida
     */
    public void setBoard(TableroDamas board) {
        this.board = board;
    }
}