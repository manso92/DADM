package com.manso92.damas.firebase;

import android.util.Log;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONObject;

import com.manso92.damas.events.NewMessageEvent;
import com.manso92.damas.view.activities.Jarvis;

/**
 * Clase que se encargará de manejar el itentfilter que detecta los nuevos mensajes que nos manda el
 * servidor y de enviarlos donde sean necesarios
 *
 * @author Pablo Manso
 * @version 08/05/2017
 */
public class MessageReceiver extends FirebaseMessagingService {

    /**
     * Tag para escribir en el log
     */
    public static final String DEBUG = "Damas.MsgReceiver";

    /**
     * Detecta los mensajes enviados y los procesa, haciéndolos llegar a quien corresponda
     * @param remoteMessage Mensaje recibido
     */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (remoteMessage.getData().size() > 0) {
            try {
                // Creamos un objeto JSON de la respuesta del mensaje
                JSONObject o = new JSONObject(remoteMessage.getData());
                
                // Creamos un mensaje eventbus con el mensaje que viene del servidor
                NewMessageEvent msg =
                        new NewMessageEvent(o.getInt("msgtype"), o.getString("sender"), o.getString("content"));
                Log.d(DEBUG, "Message data: " + msg.toString());

                // Enviamos el mensaje a EventBus
                Jarvis.event().post(msg);
            } catch (Exception e){}
        }
    }
}
