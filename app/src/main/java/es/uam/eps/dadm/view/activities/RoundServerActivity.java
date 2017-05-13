package es.uam.eps.dadm.view.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.uam.eps.dadm.R;
import es.uam.eps.dadm.events.ShowMsgEvent;
import es.uam.eps.dadm.model.Round;
import es.uam.eps.dadm.view.adapters.ViewPagerAdapter;
import es.uam.eps.dadm.view.fragment.MessageFragment;
import es.uam.eps.dadm.view.fragment.RoundFragment;

/**
 * RoundActivity es una clase que cargará el framgento de la partida cuando la pantalla del dispositivo
 * no sea lo suficientemente grande como para contener pantalla dividida
 *
 * @author Pablo Manso
 * @version 13/03/2017
 */
public class RoundServerActivity extends AppCompatActivity  {

    /**
     * Tag para escribir en el log
     */
    public static final String DEBUG = "Damas.RoundAct";

    /**
     * Barra superior por defecto de las vistas android
     */
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    /**
     * Pestañas que indicarán los nombres de las páginas que tenemos
     */
    @BindView(R.id.tabs)
    TabLayout tabLayout;

    /**
     * Contenedos de las páginas que crearemos
     */
    @BindView(R.id.viewpager)
    ViewPager viewPager;

    /**
     * Ronda que se va a jugar
     */
    private Round round;

    /**
     * Crea todo lo necesario para la correcta ejecución de la actividad
     * @param savedInstanceState Pares clave valor que nos pasa la actividad que nos invoca
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Cargamos el layout y hacemos binding de las vistas que necesitamos
        setContentView(R.layout.activity_viewpager);
        ButterKnife.bind(this);

        // Colocamos el título a la barra superior y la colocamos en la vista
        toolbar.setTitle(getString(R.string.round_title));
        setSupportActionBar(toolbar);

        // Creamos una ronda a través de los datos del intent
        this.round = Round.intentToRound(getIntent());

        // Configuramos el contenido del viewpager y cargamos esos datos en el tablayout
        setupViewPager(viewPager);
        viewPager.setPageMargin(64);
        tabLayout.setTabMode(TabLayout.MODE_FIXED);
        tabLayout.setupWithViewPager(viewPager);
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
     * Carga los fragmentos que vamos a utilizar en el viewpager
     * @param viewPager Viewpager en el que cargaremos los fragmentos
     */
    private void setupViewPager(ViewPager viewPager) {
        // Creamos un adapter que contendrá nuestros fragmentos
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        // Añadimos los fragmentos que vamos a tener en nuestro adapter
        adapter.addFragment(RoundFragment.newInstance(this.round), "Partida");
        adapter.addFragment(MessageFragment.newInstance(this.round.getId(), true), "Mensajes");

        // Vinculamos el adapter al viewpager
        viewPager.setAdapter(adapter);
    }

    /**
     * Crea un intent que contiene los datos que la acitividad necesitará para ejecutarse
     * @param packageContext Actividad que nos invocará
     * @param round Partida que esta actividad mostrará
     * @return Intenta para invocar esta actividad
     */
    public static Intent newIntent(Context packageContext, Round round) {
        // Creamos un intent entre el contexto que nos pasan y esta clase
        Intent intent = new Intent(packageContext, RoundServerActivity.class);
        return round.roundToIntent(intent);
    }

    /**
     * Captura los mensajes que se reciben por Firebase para mostrar los mensajes recibidos
     * @param msg Mensaje que contiene todos los datos necesarios para empezar un chat
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ShowMsgEvent msg){ msg.show(viewPager); }
}