package es.uam.eps.dadm.helpers;

import  org.greenrobot.eventbus.EventBus;

/**
 * Clase que se manejará el envío de mensajes internos dentro de la aplicación
 *
 * @author Pablo Manso
 * @version 08/05/2017
 */
public class GreenRobotEventBus{

    /**
     * Instancia del singleton
     */
    private static GreenRobotEventBus greenRobotEventBus;

    /**
     * Instancia de la librería EventBus
     */
    private EventBus eventBus;

    /**
     * Constructor privado de la clase
     */
    private GreenRobotEventBus() {
        // Cogemos la instancia de la librería
        this.eventBus =  EventBus.getDefault();
    }

    /**
     * Función que nos devuleve la instancia del bus para mandar eventos
     * @return Instancia del bus de eventos
     */
    public static GreenRobotEventBus getInstance() {
        if (greenRobotEventBus == null) greenRobotEventBus = new GreenRobotEventBus();
        return greenRobotEventBus;
    }

    /**
     * Registra un nuevo listener en la librería
     * @param subscriber Subscriptor que manejará el e vento
     */
    public void register(Object subscriber) {
        eventBus.register(subscriber);
    }

    /**
     * Borra del registro un listener de la librería
     * @param subscriber Subscriptor que tendremos que quitar de la librería
     */
    public void unregister(Object subscriber) {
        eventBus.unregister(subscriber);
    }

    /**
     * Enviamos un mensaje para que lo reciba un listener que se subscribió
     * @param subscriber Mensaje a enviar
     */
    public void post(Object subscriber) {
        eventBus.post(subscriber);
    }
}
