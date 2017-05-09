package es.uam.eps.dadm.helpers;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Clase que se encargará de manejar el itentfilter que detecta el cambio de token de la app
 *
 * @author Pablo Manso
 * @version 08/05/2017
 */
public class TokenRefresh  extends FirebaseInstanceIdService {

    /**
     * Debug tag
     */
    private static final String DEBUG = "Checkers.TokenRefresh";

    /**
     * Nos indica que el token de Firebase ha cambiado y tenemos que refrescarlo
     */
    @Override
    public void onTokenRefresh() {
        // Cogemos el nuevo token que nos ha asignado Firebase
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(DEBUG, "Refreshed token: " + refreshedToken);

        // Mandamos el nuevo token al servidor del juego
        sendRegistrationToServer(refreshedToken);
    }

}
