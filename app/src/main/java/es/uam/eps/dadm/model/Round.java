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
    private String id;
    private String randomPlayer;
    private String playerName;
    private String playerUUID;
    private String title;
    private String date;
    private TableroDamas board;
    private int size;


    /**
     * Constructor para una partida
     */
    public Round(int size) {
        // Generamos un nombre e identifiador de forma aleatoria
        this.id = UUID.randomUUID().toString();
        this.title = "ROUND " + id.toString().substring(19, 23).toUpperCase();
        // Ajustamos la fecha actual y creamos un nuevo tablero para la partida
        this.date = new Date().toString();
        this.board = new TableroDamas(size);
        this.randomPlayer = "Random player";
        this.size = size;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public String getPlayerUUID() {
        return playerUUID;
    }

    public void setPlayerUUID(String playerUUID) {
        this.playerUUID = playerUUID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public TableroDamas getBoard() { return board; }

    public void setBoard(TableroDamas board) {
        this.board = board;
    }

    public int getSize() {
        return size;
    }
}