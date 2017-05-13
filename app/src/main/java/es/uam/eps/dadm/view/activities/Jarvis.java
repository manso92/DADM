package es.uam.eps.dadm.view.activities;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import org.greenrobot.eventbus.EventBus;

import java.security.MessageDigest;

/**
 * Clase con funciones auxiliares que tiene que ver con distintos componentes de los recursos de
 * Android como comprobar conexión con Internet, manejras el teclado...
 *
 * @author Pablo Manso
 * @version 12/04/2017
 */
public class Jarvis {

    /**
     * Tag para escribir en el log
     */
    public static final String DEBUG = "Damas.Jarvis";


    /**
     * Función que nos devuleve la instancia del bus para mandar eventos
     * @return Instancia del bus de eventos
     */
    public static EventBus event() {
        return EventBus.getDefault();
    }

    /**
     * Calcula la función hash md5 de una cadena de texto para una contraseña
     * @param message Mensaje del que calcular el hash
     * @return Cadena hash del mensaje recibido
     */
    public static String md5Java(String message){
        String digest = null;
        MessageDigest md = null;
        byte[] hash = new byte[0];
        try {
            md = MessageDigest.getInstance("MD5");
            hash = md.digest(message.getBytes("UTF-8"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        StringBuilder sb = new StringBuilder(2*hash.length);
        for(byte b : hash) sb.append(String.format("%02x", b&0xff));

        digest = sb.toString();
        return digest;
    }


    /**
     * Indica si el móvil tiene conexión con internet o no
     * @param context Contexto desde el que se la invoca
     * @return Si el dispositivo está conectado a internet o no
     */
    static boolean isOnline(Context context) {
        ConnectivityManager connMgr = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    /**
     * Oculta el teclado del teléfono si está mostrado en la pantalla
     * @param activity Actividad desde la cual se quiere ocultar el teclado
     */
    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        if (view == null) view = new View(activity);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
