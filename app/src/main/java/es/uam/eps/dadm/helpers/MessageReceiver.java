package es.uam.eps.dadm.helpers;

import android.util.Log;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Clase que se encargará de manejar el itentfilter que detecta los nuevos mensajes que nos manda el
 * servidor y de enviarlos donde sean necesarios
 *
 * @author Pablo Manso
 * @version 08/05/2017
 */
public class MessageReceiver extends FirebaseMessagingService {

    /**
     * Debug DEBUG
     */
    private static final String DEBUG = "Checkers.MsgReceiver";

    /**
     * Detecta los mensajes enviados y los procesa, haciéndolos llegar a quien corresponda
     * @param remoteMessage Mensaje recibido
     */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        Log.d(DEBUG, "From: " + remoteMessage.getFrom());

        if (remoteMessage.getData().size() > 0) {
            Log.d(DEBUG, "Message data payload: " + remoteMessage.getData());
        }
        if (remoteMessage.getNotification() != null) {
            Log.d(DEBUG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }

        Log.d(DEBUG, "FCM Message Id: " + remoteMessage.getMessageId());
        Log.d(DEBUG, "FCM Notification Message: " + remoteMessage.getNotification());
        Log.d(DEBUG, "FCM Data Message: " + remoteMessage.getData());
        Log.d(DEBUG, "FCM From: " + remoteMessage.getFrom());
        Log.d(DEBUG, "FCM To: " + remoteMessage.getTo());
        Log.d(DEBUG, "FCM CollapseKey: " + remoteMessage.getCollapseKey());
        Log.d(DEBUG, "FCM Message Type: " + remoteMessage.getMessageType());
        Log.d(DEBUG, "FCM To String: " + remoteMessage.toString());
    }
}
