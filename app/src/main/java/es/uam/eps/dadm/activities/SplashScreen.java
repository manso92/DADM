package es.uam.eps.dadm.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import es.uam.eps.dadm.R;

/**
 * SplashScreen es una pantalla que se muestra para que la app pueda cargar todos los recurso
 * necesarios danto tiempo para que luego vaya más fluida
 *
 * @author Pablo Manso
 * @version 12/04/2017
 */
public class SplashScreen extends Activity {

    /**
     * Timeout por defecto después del cual se llamará a la actividad de login
     */
    private static int SPLASH_TIME_OUT = 3000;

    /**
     * Crea todo lo necesario para la correcta ejecución de la actividad
     * @param savedInstanceState Pares clave valor que nos pasa la actividad que nos invoca
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Cargamos el layout de la pantalla de espera
        setContentView(R.layout.activity_splash);

        // Retrasamos la ejecución del nuevo intent SPLASH_TIME_OUT milisegundos
        new Handler().postDelayed(new Runnable() {

            /**
             * Codigo que se ejecutará después de pasado el timeout
             */
            @Override
            public void run() {
                // Cresmos un intent con la siguiente actividad y la arrancamos
                Intent i = new Intent(SplashScreen.this, LoginActivity.class);
                startActivity(i);

                // Fiinalizamos esta actividad para que no volvamos a ella al retroceder
                finish();
            }
        }, SPLASH_TIME_OUT);
    }

}