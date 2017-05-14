package com.manso92.damas.events;

/**
 * Clase que se encargará de indicar el nombre de usuario con el que queremos empezar a chatear
 *
 * @author Pablo Manso
 * @version 13/05/2017
 */
public class NewChatEvent {

    /**
     * Tag para escribir en el log
     */
    public static final String DEBUG = "Damas.Event.NChat";

    /**
     * Nombre de usuario con quien empezar la conversación
     */
    private String user;

    /**
     * Construye un evento con el nombre de usuario
     * @param user Nombre de usuario con el que se va a empezar a chatear
     */
    public NewChatEvent(String user) { this.user = user; }


    public String getUser() {
        return user;
    }
    public void setUser(String user) {
        this.user = user;
    }
}
