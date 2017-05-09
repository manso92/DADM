package es.uam.eps.dadm.events;

/**
 * Clase que se encargará de contener los datos de los mensajes que nos lleguen por parte de firebase
 * para que estos sean enviados a través de EventBus
 *
 * @author Pablo Manso
 * @version 08/05/2017
 */
public class NewMessageEvent {

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
     * UUID identificador de la ronda del mensaje
     */
    private String uuid;

    /**
     * Nombre de la persona que nos manda el mensaje
     */
    private String user;

    /**
     * Mensaje que se nos manda a través del evento
     */
    private String data;

    /**
     * Tipo de evento que hemos recibido
     */
    private int eventType;


    // GETTERS Y SETTERS
    public String getUuid() {
        return uuid;
    }
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
    public String getData() {
        return data;
    }
    public void setData(String data) {
        this.data = data;
    }
    public int getEventType() {
        return eventType;
    }
    public void setEventType(int eventType) {
        this.eventType = eventType;
    }
}
