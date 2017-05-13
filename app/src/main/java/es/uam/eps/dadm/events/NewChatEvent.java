package es.uam.eps.dadm.events;

/**
 * Clase que se encargará de indicar el nombre de usuario con el que queremos empezar a chatear
 *
 * @author Pablo Manso
 * @version 13/05/2017
 */
public class NewChatEvent {

    /**
     * Nombre de usuario con quien empezar la conversación
     */
    private String user;

    public NewChatEvent(String user) { this.user = user; }
    public String getUser() {
        return user;
    }
    public void setUser(String user) {
        this.user = user;
    }
}
