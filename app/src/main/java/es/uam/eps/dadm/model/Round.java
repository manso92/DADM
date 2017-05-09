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
     * Tipos de partida que puede haber y su estado
     */
    public enum Type{LOCAL, OPEN, ACTIVE, FINISHED};

    /**
     * Identificador de la partida
     */
    private String id;

    /**
     * Título de la partida
     */
    private String title;

    /**
     * Tipo o estado de la partida
     */
    private Type tipo;

    /**
     * Fecha de creación de la partida
     */
    private String date;

    /**
     * Tablero de la partida
     */
    private TableroDamas board;

    /**
     * Nombre del primer jugador
     */
    private String firstUserName;

    /**
     * UUID del primer jugador
     */
    private String firstUserUUID;

    /**
     * Nombre del segundo jugador
     */
    private String secondUserName;

    /**
     *  UUID del segundo jugador
     */
    private String secondUserUUID;


    /**
     * Constructor para una partida
     * @param tipo Tipo de la partida
     * @param size Tamaño de la partida
     */
    public Round(int size, Type tipo) {
        this.setId(UUID.randomUUID().toString());
        this.tipo = tipo;
        this.date = new Date().toString();
        this.board = new TableroDamas(size);
    }

    /**
     * Constructor de una partida
     * @param id Identificador de la partida
     * @param tipo Tipo de la partida
     * @param date Fecha de la partida
     * @param size Tamaño de la partida
     */
    public Round(String id, Round.Type tipo, String date, int size) {
        this.setId(id);
        this.tipo = tipo;
        this.date = date;
        this.board = new TableroDamas(size);
    }

    /**
     * Constructor de una partida
     * @param id Identificador de la partida
     * @param tipo Tipo de la partida
     * @param date Fecha de la partida
     * @param size Tamaño de la partida
     */
    public Round(int id, Round.Type tipo, String date, int size) {
        this.setId(id);
        this.tipo = tipo;
        this.date = date;
        this.board = new TableroDamas(size);
    }


    public String getId() { return id;}
    public void setId(int id) {this.id = Integer.toString(id); this.title = titleFromID(id);}
    public void setId(String id) {this.id = id; this.title = titleFromID(id);}

    public String getTitle() {return title;}
    private String titleFromID(int id) {return "ROUND " + Integer.toString(id);}
    private String titleFromID(String id) {
        if (id.length() > 23)
            return "ROUND " + id.toString().substring(19, 23).toUpperCase();
        return "ROUND " + id;
    }

    public Type getTipo() {return tipo;}
    public void setTipo(Type tipo) {this.tipo = tipo;}

    public String getDate() {return date;}
    public void setDate(String date) {this.date = date;}

    public int getSize() {return board.size;}
    public TableroDamas getBoard() {return board;}
    public void setBoard(TableroDamas board) {this.board = board;}

    public int turn (String username){
        if (this.getFirstUserName().equals(username))
            return 1;
        if (this.getSecondUserName().equals(username))
            return 2;
        return 0;
    }

    public String getFirstUserName() {return firstUserName;}
    public String getFirstUserUUID() {return firstUserUUID;}
    public void setFirstUser(String firstUserName, String firstUserUUID) {
        this.firstUserUUID = firstUserUUID;
        this.firstUserName = firstUserName;
    }
    public void setUserRandom() {
        this.firstUserUUID = Preferences.PLAYERUUID_DEFAULT;
        this.firstUserName = Preferences.PLAYERNAME_DEFAULT;
    }

    public String getSecondUserName() {return secondUserName;}
    public String getSecondUserUUID() {return secondUserUUID;}
    public void setSecondUser(String secondUserName,String secondUserUUID) {
        this.secondUserUUID = secondUserUUID;
        this.secondUserName = secondUserName;
    }
}