package es.uam.eps.dadm.view.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.uam.eps.dadm.R;
import es.uam.eps.dadm.model.Preferences;
import es.uam.eps.dadm.model.Round;
import es.uam.eps.dadm.model.RoundRepository;
import es.uam.eps.dadm.model.RoundRepositoryFactory;
import es.uam.eps.dadm.server.ServerRepository;
import es.uam.eps.dadm.view.fragment.BlankFragment;
import es.uam.eps.dadm.view.fragment.RoundListFragment;

/**
 * RoundListActivity es una pantalla que muestra las partidas disponibles para el usuario, de momento
 * tres
 * - Lista de partidas locales disponibles
 * - Lista de partidas en el servidor
 * - Lista de partidas disponibles para empezar
 *
 * @author Pablo Manso
 * @version 02/05/2017
 */
public class RoundListActivity extends AppCompatActivity implements RoundListFragment.Callbacks {

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

        // Colocamos el título a la barra superior y la colocamos en la vista
        toolbar.setTitle(getString(R.string.round_title));
        setSupportActionBar(toolbar);

        // Configuramos el contenido del viewpager y cargamos esos datos en el tablayout
        setupViewPager(viewPager);
        viewPager.setPageMargin(64);
        tabLayout.setupWithViewPager(viewPager);
    }

    /**
     * Carga los fragmentos que vamos a utilizar en el viewpager
     * @param viewPager Viewpager en el que cargaremos los fragmentos
     */
    private void setupViewPager(ViewPager viewPager) {
        // Creamos un adapter que contendrá nuestros fragmentos
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        // Añadimos los fragmentos que vamos a tener en nuestro adapter
        adapter.addFragment(RoundListFragment.newInstance(Round.Type.LOCAL), "Local");
        adapter.addFragment(RoundListFragment.newInstance(Round.Type.ACTIVE), "Mis partidas");
        adapter.addFragment(RoundListFragment.newInstance(Round.Type.OPEN), "Partidas abiertas");
        adapter.addFragment(RoundListFragment.newInstance(Round.Type.FINISHED), "Finalizadas");
        adapter.addFragment(new BlankFragment(), "Estadísticas");

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
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Miramos que botón del menú ha pulsado
        switch (item.getItemId()) {
            case R.id.menu_item_settings:
                // Abrimos las preferencias para que cambie los valores de los juegos
                startActivity(new Intent(this, Preferences.class));
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
     *
     * @param round Partida que el jugador ha seleccionado
     */
    @Override
    public void onRoundSelected(Round round, Round.Type tipo) {
        switch (tipo){
            case LOCAL:
                startActivity(RoundActivity.newIntent(this, round));
                break;
            case OPEN:
                RoundRepository server = RoundRepositoryFactory.createRepository(this,true);
                RoundRepository.BooleanCallback callback = new RoundRepository.BooleanCallback() {
                    @Override
                    public void onResponse(boolean ok) {
                        if (ok) {
                            Snackbar.make(viewPager, R.string.repository_round_add_user_success,
                                    Snackbar.LENGTH_LONG).show();
                            ((RoundListFragment)((ViewPagerAdapter)viewPager.getAdapter()).getItem(1)).updateUI();
                            ((RoundListFragment)((ViewPagerAdapter)viewPager.getAdapter()).getItem(2)).updateUI();
                        }
                        else
                            Snackbar.make(viewPager, R.string.repository_round_add_user_success,
                                Snackbar.LENGTH_LONG).show();
                    }
                };
                ((ServerRepository) server).addPlayerToRound(round, Preferences.getPlayerUUID(this),callback);
                break;
            case ACTIVE:
                startActivity(RoundActivity.newIntent(this, round));
                break;
            case FINISHED:
                break;
        }

    }

    /**
     * ViewPagerAdapter manejará las páginas que tendrá nuestro viewpage, permitiento añadir nuevos
     * fragments a nuestro contenedor y poder mostrar las partidas
     */
    class ViewPagerAdapter extends FragmentPagerAdapter {
        /**
         * Lista de fragmentos que tendrá nuestro viewpager
         */
        private final List<Fragment> mFragmentList = new ArrayList<>();

        /**
         * Titulos de los fragmentos que se verá en el tab
         */
        private final List<String> mFragmentTitleList = new ArrayList<>();

        /**
         * Construgtor de la clase
         *
         * @param manager FragmentManager de los fragments que tendrá nuestro viewpger
         */
        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        /**
         * Devuelve el fragmento que esta en una posición determinada
         *
         * @param position Posición del fragmento que queremos rescatar
         * @return Fragmento rescatado de la posición indicada
         */
        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        /**
         * Devuelve el titulo del fragmento que esta en una posición determinada
         *
         * @param position Posición del fragmento del que queremos obtener el título
         * @return Título del fragmento que está en la posición indicada
         */
        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }

        /**
         * Indica el número de páginas que tendrá nuestro ViewPager
         *
         * @return Número de páginas
         */
        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        /**
         * Añade un nuevo fragmento a nuestro ViewPager
         *
         * @param fragment Fragmento a añadir
         * @param title    Título del fragmento
         */
        public void addFragment(Fragment fragment, String title) {
            // Añadimos el fragmento y el título a las listas donde se almacenan
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
            // Indicamos a la clase superior que han cambiado las páginas
            this.notifyDataSetChanged();
        }

        /**
         * Devuelve el ancho que tendrá la página
         * @param position Posición del fragment del que queremos el ancho
         * @return Ancho de la página que solicitábamos
         */
        @Override
        public float getPageWidth(int position) {
            return getResources().getBoolean(R.bool.viewPagerWidthTablet) ? 1f : 1f;
        }
    }
}
