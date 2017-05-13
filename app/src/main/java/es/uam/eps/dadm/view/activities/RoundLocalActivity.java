package es.uam.eps.dadm.view.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.FrameLayout;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import es.uam.eps.dadm.R;
import es.uam.eps.dadm.events.ShowMsgEvent;
import es.uam.eps.dadm.model.Round;
import es.uam.eps.dadm.view.fragment.RoundFragment;

/**
 * RoundActivity es una clase que cargará el framgento de la partida cuando la pantalla del dispositivo
 * no sea lo suficientemente grande como para contener pantalla dividida
 *
 * @author Pablo Manso
 * @version 13/03/2017
 */
public class RoundLocalActivity extends AppCompatActivity  {

    /**
     * Tag para escribir en el log
     */
    public static final String DEBUG = "Damas.RoundAct";

    /**
     * Fragment principal donde cargaremos el contenido
     */
    @BindView(R.id.fragment_container)
    FrameLayout fragmentContainer;

    /**
     * Crea un intent que contiene los datos que la acitividad necesitará para ejecutarse
     * @param packageContext Actividad que nos invocará
     * @param round Partida que esta actividad mostrará
     * @return Intenta para invocar esta actividad
     */
    public static Intent newIntent(Context packageContext, Round round) {
        // Creamos un intent entre el contexto que nos pasan y esta clase
        Intent intent = new Intent(packageContext, RoundLocalActivity.class);
        return round.roundToIntent(intent);
    }

    /**
     * Crea todo lo necesario para la correcta ejecución de la actividad
     * @param savedInstanceState Pares clave valor que nos pasa la actividad que nos invoca
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Llamamos a la clase padre
        super.onCreate(savedInstanceState);
        // Cargamos el layout de la acitividad
        setContentView(R.layout.activity_fragment);

        // Creamos una ronda a través de los datos del intent
        Round round = Round.intentToRound(getIntent());

        // Cogemos el fragment manager y cargamos el fragment
        FragmentManager fm = getSupportFragmentManager();
        RoundFragment roundFragment = RoundFragment.newInstance(round);
        fm.beginTransaction().add(R.id.fragment_container, roundFragment).commit();
    }

    /**
     * Ejecución al inicio del fragmento
     */
    @Override
    public void onStart() {
        super.onStart();

        // Empezamos a capturar los eventos
        Jarvis.event().register(this);
    }

    /**
     * Ejecución al final del fragmento
     */
    @Override
    public void onStop() {
        super.onStop();

        // Dejamos de campturar eventos
        Jarvis.event().unregister(this);
    }

    /**
     * Captura los mensajes que se reciben por Firebase para mostrar los mensajes recibidos
     * @param msg Mensaje que contiene todos los datos necesarios para empezar un chat
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ShowMsgEvent msg){ msg.show(fragmentContainer); }

}