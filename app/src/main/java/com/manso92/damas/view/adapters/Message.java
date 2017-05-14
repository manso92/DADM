package com.manso92.damas.view.adapters;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Esta clase almacenará los datos de un mensaje que se ha enviado al servidor
 *
 * @author Pablo Manso
 * @version 11/05/2017
 */
public class Message {

    /**
     * Nombre de usuario que manda el mensaje
     */
    private String fromName;

    /**
     * Mensaje que se ha enviado
     */
    private String message;

    /**
     * Si el mensaje es propio o no
     */
    private boolean isSelf;

    /**
     * Fecha en la que se envío el mensaje
     */
    private Date date;

    /**
     * Constructor vacío para un mensaje
     */
    public Message() {}


    /**
     * Constructor para un nuevo mensaje
     * @param fromName Nombre de quien manda el mensaje
     * @param message Mensaje que manda
     * @param isSelf Si es propio o si es ajeno
     */
    public Message(String fromName, String message, boolean isSelf) {
        this.fromName = fromName;
        this.message = message;
        this.isSelf = isSelf;
        this.date= new Date();
    }

    /**
     * Constructor para un nuevo mensaje
     * @param fromName Nombre de quien manda el mensaje
     * @param message Mensaje que manda
     * @param isSelf Si es propio o si es ajeno
     * @param date Fecha en la que se mandó el mensaje
     */
    public Message(String fromName, String message, boolean isSelf, String date) {
        this.fromName = fromName;
        this.message = message;
        this.isSelf = isSelf;

        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
        try {
            this.date = format.parse(date);
        } catch (Exception e){ e.printStackTrace(); }
    }

    // GETTERS Y SETTERS
    public String getFromName() {
        return fromName;
    }
    public void setFromName(String fromName) {
        this.fromName = fromName;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public boolean isSelf() {
        return isSelf;
    }
    public void setSelf(boolean isSelf) {
        this.isSelf = isSelf;
    }
    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }
}
