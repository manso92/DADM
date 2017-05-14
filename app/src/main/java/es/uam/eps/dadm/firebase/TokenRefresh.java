package es.uam.eps.dadm.firebase;

import android.content.Context;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import es.uam.eps.dadm.model.Preferences;
import es.uam.eps.dadm.model.RoundRepository;
import es.uam.eps.dadm.server.ServerRepository;

/**
 * Clase que se encargará de manejar el itentfilter que detecta el cambio de token de la app
 *
 * @author Pablo Manso
 * @version 15/05/2017
 */
public class TokenRefresh extends FirebaseInstanceIdService {

    /**
     * Tag para escribir en el log
     */
    public static final String DEBUG = "Damas.TokenRefresh";

    /**
     * Actualiza el token con el que nos registramos en el servidor de forma estática
     * @param context Contexto desde el que se refresca el token
     */
    public static void refreshToken(Context context){
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        ServerRepository serverRepository = ServerRepository.getInstance(context);
        RoundRepository.LoginRegisterCallback callback = new RoundRepository.LoginRegisterCallback(){
            @Override
            public void onLogin(String playerUuid) {}
            @Override
            public void onError(String error) { }
        };
        Preferences.setFirebaseToken(context, refreshedToken);
        serverRepository.login(Preferences.getPlayerName(context),
                Preferences.getPlayerPassword(context), callback);
    }

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

    /**
     * Actualiza el token con el que registrarse en el servidor
     * @param token Nuevo token de Firebase
     */
    private void sendRegistrationToServer(String token) {
        ServerRepository serverRepository = ServerRepository.getInstance(getApplicationContext());
        RoundRepository.LoginRegisterCallback callback = new RoundRepository.LoginRegisterCallback(){
            @Override
            public void onLogin(String playerUuid) {}
            @Override
            public void onError(String error) { }
        };
        Preferences.setFirebaseToken(getApplicationContext(), token);
        serverRepository.login(Preferences.getPlayerName(getApplicationContext()),
                Preferences.getPlayerPassword(getApplicationContext()), callback);
    }
}
