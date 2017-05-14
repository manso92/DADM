package com.manso92.damas.events;

/**
 * Clase que se encargará de contener los datos de los mensajes que nos lleguen por parte de firebase
 * para que estos sean enviados a través de EventBus
 *
 * @author Pablo Manso
 * @version 08/05/2017
 */
public class NewMessageEvent {

    /**
     * Tag para escribir en el log
     */
    public static final String DEBUG = "Damas.Event.NMess";

    /**
     * El mensaje que nos llega es un movimiento de una nueva ronda
     */
    public static final int newMovement = 1;

    /**
     * El mensaje es un nuevo mensaje de un usuario
     */
    public static final int userMessage = 2;

    /**
     * El mensaje es un nuevo mensaje de una ronda
     */
    public static final int roundMessage = 3;

    /**
     * Tipo de evento que hemos recibido
     */
    private int msgtype;

    /**
     * UUID identificador del mensaje
     */
    private String sender;

    /**
     * Mensaje que se nos manda a través del evento
     */
    private String content;

    /**
     * Constructor vacío del evento
     */
    public NewMessageEvent() {}

    /**
     * Constructor de un mensaje con todos sus datos
     * @param msgtype Tipo de mensaje que he mos recibido
     * @param sender Quien envía el mensaje o a donde va dirigido
     * @param content Contenido del mensaje
     */
    public NewMessageEvent(int msgtype, String sender, String content) {
        this.msgtype = msgtype;
        this.sender = sender;
        this.content = content;
    }

    // GETTERS Y SETTERS
    public int getMsgtype() { return msgtype; }
    public void setMsgtype(int msgtype) { this.msgtype = msgtype; }
    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content;}

    public String toString(){
        return "Mensaje de tipo " + this.getMsgtype() + " con destinatario " + this.getSender() +
                "con el mensaje: " + this.getContent();
    }
}
