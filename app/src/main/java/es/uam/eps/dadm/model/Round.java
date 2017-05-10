package es.uam.eps.dadm.model;

import android.content.Intent;
import android.os.Bundle;

import java.util.Date;
import java.util.UUID;

import es.uam.eps.multij.ExcepcionJuego;

/**
 * Esta clase almacenará los datos de una partida de la aplicación
 *
 * @author Pablo Manso
 * @version 13/03/2017
 */
public class Round {

    /**
     * Tag para escribir en el log
     */
    public static final String DEBUG = "Damas.Round";

    // TAGS PARA SERIALIZACIÓN
    private static final String ARG_ROUND_ID = "es.uam.eps.dadm.round_id";
    private static final String ARG_FIRST_PLAYER_NAME = "es.uam.eps.dadm.first_player_name";
    private static final String ARG_FIRST_PLAYER_UUID = "es.uam.eps.dadm.first_player_uuid";
    private static final String ARG_SECOND_PLAYER_NAME = "es.uam.eps.dadm.second_player_name";
    private static final String ARG_SECOND_PLAYER_UUID = "es.uam.eps.dadm.second_player_uuid";
    private static final String ARG_ROUND_TYPE = "es.uam.eps.dadm.round_type";
    private static final String ARG_ROUND_SIZE = "es.uam.eps.dadm.round_size";
    private static final String ARG_ROUND_DATE = "es.uam.eps.dadm.round_date";
    private static final String ARG_ROUND_BOARD = "es.uam.eps.dadm.round_board";

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

    /**
     * Introduce todos las propiedades de una partida en un intent para pasarlo de una activity a otra
     * @param intent Intent en el que cargar los datos
     * @return El intent con todos los parámetros añadidos
     */
    public Intent roundToIntent(Intent intent){
        // Adjuntamos la ronda con la clave que tenemos en la clase y devolvemos el intent
        intent.putExtra(ARG_ROUND_ID, this.getId());
        intent.putExtra(ARG_ROUND_TYPE, this.getTipo());
        intent.putExtra(ARG_FIRST_PLAYER_NAME, this.getFirstUserName());
        intent.putExtra(ARG_FIRST_PLAYER_UUID, this.getFirstUserUUID());
        intent.putExtra(ARG_SECOND_PLAYER_NAME, this.getSecondUserName());
        intent.putExtra(ARG_SECOND_PLAYER_UUID, this.getSecondUserUUID());
        intent.putExtra(ARG_ROUND_SIZE, Integer.toString(this.getSize()));
        intent.putExtra(ARG_ROUND_DATE, this.getDate());
        intent.putExtra(ARG_ROUND_BOARD, this.getBoard().tableroToString());
        return intent;
    }

    /**
     * Recoge todas las propiedades de una ronda de un intent y se la devuelve ya creada
     * @param intent Intent del que obtener los datos
     * @return Ronda creada con los datos introducidos
     */
    public static Round intentToRound(Intent intent){
        // TODO comprobar si tiene todas las claves necesarias
        Round round = new Round(intent.getStringExtra(ARG_ROUND_ID),
                (Round.Type) intent.getSerializableExtra(ARG_ROUND_TYPE),
                intent.getStringExtra(ARG_ROUND_DATE),
                Integer.parseInt(intent.getStringExtra(ARG_ROUND_SIZE)));

        round.setFirstUser(intent.getStringExtra(ARG_FIRST_PLAYER_NAME),
                intent.getStringExtra(ARG_FIRST_PLAYER_UUID));
        round.setSecondUser(intent.getStringExtra(ARG_SECOND_PLAYER_NAME),
                intent.getStringExtra(ARG_SECOND_PLAYER_UUID));

        try {
            round.getBoard().stringToTablero(intent.getStringExtra(ARG_ROUND_BOARD));
        } catch (ExcepcionJuego excepcionJuego) {
            excepcionJuego.printStackTrace();
            return null;
        }
        return round;
    }

    /**
     * Introduce todos las propiedades de una partida en un bundle para pasarlo de una activity a un fragment
     * @param args Bundle en el que cargar los datos
     * @return El bundle con todos los parámetros añadidos
     */
    public Bundle roundToBundle(Bundle args){
        // Ponemos uno a uno todos los argumentos en el contenedor
        args.putString(ARG_ROUND_ID, this.getId());
        args.putSerializable(ARG_ROUND_TYPE, this.getTipo());
        args.putString(ARG_FIRST_PLAYER_NAME, this.getFirstUserName());
        args.putString(ARG_FIRST_PLAYER_UUID, this.getFirstUserUUID());
        args.putString(ARG_SECOND_PLAYER_NAME, this.getSecondUserName());
        args.putString(ARG_SECOND_PLAYER_UUID, this.getSecondUserUUID());
        args.putString(ARG_ROUND_SIZE, Integer.toString(this.getSize()));
        args.putString(ARG_ROUND_DATE, this.getDate());
        args.putString(ARG_ROUND_BOARD, this.getBoard().tableroToString());
        return args;
    }

    /**
     * Recoge todas las propiedades de una ronda de un bundle y se la devuelve ya creada
     * @param args Bundle del que obtener los datos
     * @return Ronda creada con los datos introducidos
     */
    public static Round bundleToRound(Bundle args){
        Round round = new Round(args.getString(ARG_ROUND_ID),
                (Round.Type) args.getSerializable(ARG_ROUND_TYPE),
                args.getString(ARG_ROUND_DATE),
                Integer.parseInt(args.getString(ARG_ROUND_SIZE)));

        round.setFirstUser(args.getString(ARG_FIRST_PLAYER_NAME),
                args.getString(ARG_FIRST_PLAYER_UUID));
        round.setSecondUser(args.getString(ARG_SECOND_PLAYER_NAME),
                args.getString(ARG_SECOND_PLAYER_UUID));

        try {
            round.getBoard().stringToTablero(args.getString(ARG_ROUND_BOARD));
        } catch (ExcepcionJuego excepcionJuego) {
            excepcionJuego.printStackTrace();
            return null;
        }
        return round;
    }


    // GETTERS Y SETTERS
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