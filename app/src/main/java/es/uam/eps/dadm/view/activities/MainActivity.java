package es.uam.eps.dadm.view.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.uam.eps.dadm.R;
import es.uam.eps.dadm.events.RefreshRoundListEvent;
import es.uam.eps.dadm.events.ShowMsgEvent;
import es.uam.eps.dadm.model.Preferences;
import es.uam.eps.dadm.model.Round;
import es.uam.eps.dadm.model.RoundRepository;
import es.uam.eps.dadm.model.RoundRepositoryFactory;
import es.uam.eps.dadm.server.ServerRepository;
import es.uam.eps.dadm.view.adapters.ViewPagerAdapter;
import es.uam.eps.dadm.view.fragment.MessageListFragment;
import es.uam.eps.dadm.view.fragment.RoundListFragment;

/**
 * MainActivity es una pantalla que muestra las partidas disponibles para el usuario, de momento
 * tres
 * - Lista de partidas locales disponibles
 * - Lista de partidas en el servidor
 * - Lista de partidas disponibles para empezar
 *
 * @author Pablo Manso
 * @version 02/05/2017
 */
public class MainActivity extends AppCompatActivity implements RoundListFragment.Callbacks {

    /**
     * Tag para escribir en el log
     */
    public static final String DEBUG = "Damas.RoundListAct";

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
     * Prepara todos lo necesario para la correcta creación de la vista
     * @param savedInstanceState Pares clave valor que nos pasa la actividad que nos invoca
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Cargamos el layout y hacemos binding de las vistas que necesitamos
        setContentView(R.layout.activity_viewpager);
        ButterKnife.bind(this);

        // Empezamos a capturar los eventos
        Jarvis.eventRegister(this);

        // Colocamos el título a la barra superior y la colocamos en la vista
        toolbar.setTitle(getString(R.string.round_title));
        setSupportActionBar(toolbar);

        // Configuramos el contenido del viewpager y cargamos esos datos en el tablayout
        setupViewPager(viewPager);
        viewPager.setPageMargin(64);
        tabLayout.setupWithViewPager(viewPager);
    }
    /**
     * Ejecución al inicio del fragmento
     */
    @Override
    public void onStart() {
        super.onStart();

        // Empezamos a capturar los eventos
        Jarvis.eventRegister(this);
    }

    /**
     * Ejecución al final del fragmento
     */
    @Override
    public void onStop() {
        super.onStop();

        // Dejamos de campturar eventos
        Jarvis.eventUnregister(this);
    }

    /**
     * Carga los fragmentos que vamos a utilizar en el viewpager
     * @param viewPager Viewpager en el que cargaremos los fragmentos
     */
    private void setupViewPager(ViewPager viewPager) {
        // Creamos un adapter que contendrá nuestros fragmentos
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        // Añadimos los fragmentos que vamos a tener en nuestro adapter
        adapter.addFragment(RoundListFragment.newInstance(Round.Type.LOCAL), "Offline");
        adapter.addFragment(RoundListFragment.newInstance(Round.Type.ACTIVE), "Mis partidas");
        adapter.addFragment(RoundListFragment.newInstance(Round.Type.OPEN), "Partidas abiertas");
        adapter.addFragment(RoundListFragment.newInstance(Round.Type.FINISHED), "Finalizadas");
        adapter.addFragment(new MessageListFragment(), "Mensajes");

        // Vinculamos el adapter al viewpager
        viewPager.setAdapter(adapter);
    }

    /**
     * Crea el menú de opciones de la actividad
     * @param menu Menú en el que cargar las opciones
     * @return Correcta o no carga del menú
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    /**
     * Manejará las acciones a realizar cuando se pulsen las distintas
     * @param item Item del menú que se ha pulsado
     * @return Si se ha ejecutado correctamente o no el menú
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Miramos que botón del menú ha pulsado
        switch (item.getItemId()) {
            case R.id.menu_item_settings:
                // Abrimos las preferencias para que cambie los valores de los juegos
                startActivity(new Intent(this, PreferenceActivity.class));
                return true;
            case R.id.menu_item_help:
                // Abrimos la ayuda
                startActivity(new Intent(this, HelpActivity.class));
                return true;
            case R.id.menu_item_signout:
                // Reseteamos las preferencias
                Preferences.resetPreferences(this);
                // Arrancamos la activity del login y finalizamos esta
                startActivity(new Intent(this, LoginActivity.class));
                this.finish();
                return true;

        }
        // Si no encontramos la opción utilizada, encargamos a la clase padre que lo gestione
        return super.onOptionsItemSelected(item);
    }


    /**
     * Función que se ejecutará cuando se dispare el listener en una partida de la lista
     * @param round Partida que el jugador ha seleccionado
     */
    @Override
    public void onRoundSelected(Round round, Round.Type tipo) {
        switch (tipo) {
            case LOCAL:
                startActivity(RoundLocalActivity.newIntent(this, round));
                break;
            case OPEN:
                RoundRepository server = RoundRepositoryFactory.createRepository(this, true);
                RoundRepository.BooleanCallback callback = new RoundRepository.BooleanCallback() {
                    @Override
                    public void onResponse(boolean ok) {
                        if (ok) {
                            Jarvis.error(ShowMsgEvent.Type.SNACKBAR,
                                    R.string.repository_round_add_user_success, MainActivity.this);
                            Jarvis.event().post(new RefreshRoundListEvent(Round.Type.OPEN));
                            Jarvis.event().post(new RefreshRoundListEvent(Round.Type.ACTIVE));
                        } else
                            Jarvis.error(ShowMsgEvent.Type.SNACKBAR,
                                    R.string.repository_round_add_user_success, MainActivity.this);
                    }
                };
                ((ServerRepository) server).addPlayerToRound(round, Preferences.getPlayerUUID(this), callback);
                break;
            case ACTIVE:
                startActivity(RoundServerActivity.newIntent(this, round));
                break;
            case FINISHED:
                break;
        }
    }

    /**
     * Captura los mensajes que se reciben por Firebase para mostrar los mensajes recibidos
     * @param msg Mensaje que contiene todos los datos necesarios para empezar un chat
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ShowMsgEvent msg){ msg.show(viewPager); }
}
