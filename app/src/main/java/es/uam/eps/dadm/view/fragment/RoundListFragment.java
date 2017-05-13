package es.uam.eps.dadm.view.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.BindView;
import butterknife.OnClick;
import butterknife.Unbinder;
import es.uam.eps.dadm.R;
import es.uam.eps.dadm.model.Round;
import es.uam.eps.dadm.model.RoundRepository;
import es.uam.eps.dadm.model.RoundRepositoryFactory;
import es.uam.eps.dadm.model.Preferences;
import es.uam.eps.dadm.view.adapters.RoundAdapter;
import es.uam.eps.dadm.view.listeners.RecyclerItemClickListener;

/**
 * RoundListFragment es el fragmento que mostrará la lista de partidas necesarias
 *
 * @author Pablo Manso
 * @version 13/03/2017
 */
public class RoundListFragment extends Fragment {

    /**
     * Tag para escribir en el log
     */
    public static final String DEBUG = "Damas.RoundListFrag";

    /**
     * Instancia del recycler
     */
    @BindView(R.id.recycler_view)
    RecyclerView roundRecyclerView;

    /**
     * Instancia del botón de añadir partida
     */
    @BindView(R.id.fab)
    FloatingActionButton addFoundFab;

    /**
     * Instancia del layout para refrescar la lista de partidas
     */
    @BindView(R.id.swiperefresh)
    SwipeRefreshLayout refreshLayout;

    /**
     * Clave del parámetro del repositorio por defecto del que coger los datos
     */
    private static final String REPOSITORY_KEY  = "repository_key";

    /**
     * Clave del parámetro del filtro de las rondas que queremos dels servidor
     */
    private static final String ROUNDTYPE_KEY  = "roundtype_key";

    /**
     * Repositorio por defecto del que coger los datos
     */
    private RoundRepository repository;

    /**
     * Repositorio por defecto del que coger los datos
     */
    private Round.Type type;

    /**
     * Instancia necesaria de Butterknife para realizar el unbinding
     */
    private Unbinder unbinder;

    /**
     * Adapter que manejará nuestra lista personalizada
     */
    private RoundAdapter roundAdapter;

    /**
     * Callback al que llamar cuando se seleccione un ronda
     */
    private Callbacks callbacks;

    /**
     * Interfaz que deberá implementar la clase que quiere que le avisemos de la selección de un item
     */
    public interface Callbacks {
        void onRoundSelected(Round round, Round.Type tipo);
    }

    /**
     * Crea una nueva instancia del fragmento en el que se incluye el repositorio
     * @param type Tipo de  Repositorio de datos para el framento
     * @return Fragment con los parámetros añadidos
     */
    public static RoundListFragment newInstance(Round.Type type) {
        // Creamos un fragmento y un bundle para los argumentos
        RoundListFragment fragment = new RoundListFragment();
        Bundle bundle = new Bundle();
        // Añadimos el repositorio, colocamos los valores y devolvemos el fragmento
        bundle.putSerializable(ROUNDTYPE_KEY, type);
        fragment.setArguments(bundle);
        return fragment;
    }

    /**
     * Crea todo lo necesario para la correcta ejecución del fragmento
     * @param savedInstanceState Pares clave valor que nos pasa la actividad que nos invoca
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        // Llamamos al constructor
        super.onCreate(savedInstanceState);

        // Obtenemos el tipo de partidas que queremos
        if ((getArguments() != null) && (getArguments().containsKey(ROUNDTYPE_KEY)))
            this.type = (Round.Type) getArguments().getSerializable(ROUNDTYPE_KEY);
        else
            this.type = this.repository.getDefaultFilter();

        // Creamos un repositorio en base a lo que nos hayan mandado
        this.repository =
                RoundRepositoryFactory.createRepository(this.getContext(), this.type != Round.Type.LOCAL);
    }

    /**
     * Función que se ejecutará cuando se vuelva de una pausa
     */
    @Override
    public void onResume() {
        // Indicamos a la clase padre que hemos vuelto de la pausa y actualizamos la interfaz
        super.onResume();
        this.updateUI();
    }

    /**
     * Ejecutará las acciones necesarias para cuando se cree la vista
     * @param inflater Clase que se encargará de mostrar los elementos del fragment
     * @param container Contenedor del fragment
     * @param savedInstanceState Pares clave valor que se nos dan como parámetro
     * @return View que se acaba de crear
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Cargamos el layout del fragmento y hacemos binding de las vistas
        final View view = inflater.inflate(R.layout.fragment_recycler, container, false);
        this.unbinder = ButterKnife.bind(this, view);

        // Configuramos el recycler view y devolvemos la vista
        this.setupRecyclerView();

        // Miramos el tipo y ocultamos el botón si es necesario
        if ((this.type == Round.Type.OPEN) || (this.type == Round.Type.FINISHED))
            addFoundFab.setVisibility(View.GONE);


        // Añadimos el listener que recargará la lista mostrada
        refreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        updateUI();
                    }
                }
        );

        return view;
    }

    /**
     * Función que se ejecutará cuando se destruya la view
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Hacemos unbinding de todas las vistas que Butterknife utilizara antes
        unbinder.unbind();
    }

    /**
     * Configura el recyvlerview con la lista de partidas que toque
     */
    public void setupRecyclerView(){
        // Cogemos el layout manager y le ajustamos un linearlayout para la lista
        this.roundRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        this.roundRecyclerView.setItemAnimator(new DefaultItemAnimator());

        // Añadimos el listener de la lista
        this.roundRecyclerView.addOnItemTouchListener(new
                RecyclerItemClickListener(getActivity(), new
                RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, final int position) {
                        // Llamamos al callback con la ronda que se ha presionado
                        if (position < roundAdapter.getItemCount())
                            callbacks.onRoundSelected(roundAdapter.getRound(position), type);
                    }
                }));
    }

    /**
     * Actualiza la interfaz, la lista de las partidas disponibles
     */
    public void updateUI() {
        // Si está cargado el roundadapter, lo vaciamos
        if(this.roundAdapter != null) this.roundAdapter.clear();

        // Registramos el callback que manejará la lista de partidas devueltas
        RoundRepository.RoundsCallback roundsCallback = new RoundRepository.RoundsCallback() {
            @Override
            public void onResponse(List<Round> rounds) {
                // Si no hay adapter lo creamos, y si lo hay, las añadimos
                if (roundAdapter == null)
                    roundAdapter = new RoundAdapter(rounds);
                else
                    roundAdapter.addRounds(rounds);

                // Añadimos el adapter al recyclerview
                if (roundRecyclerView != null)
                    roundRecyclerView.setAdapter(roundAdapter);

                // Parar la animación del indicador
                if (refreshLayout != null)
                    refreshLayout.setRefreshing(false);
            }
            @Override
            public void onError(String error) {
                // Mostramos el error que nos indican al llamar al callback
                Snackbar.make(roundRecyclerView, error, Snackbar.LENGTH_LONG).show();
            }
        };
        // Regcargamos la lista de rondas disponibles
        repository.getRounds(Preferences.getPlayerUUID(this.getActivity()),
                null, this.type, roundsCallback);
    }

    /**
     * Esta función se ejecutará cuando se adhiere a un contenedor
     * @param context Contenedor en el que se adhiere el fragmento
     */
    @Override
    public void onAttach(Context context) {
        // Llamamos al padre y registramos el callback para cuando se actualiza la ronda
        super.onAttach(context);
        callbacks = (Callbacks) context;
    }

    /**
     * Esta función se ejecutará cuando se despegue del contenedor
     */
    @Override
    public void onDetach() {
        // Llamamos al padre y nos desvinculamos del callback
        super.onDetach();
        callbacks = null;
    }

    /**
     * Captura el evento click en el botón de añadir ronda
     * @param v View del botón que se pulsa
     */
    @OnClick(R.id.fab)
    public void newRound(View v) {
        // Creamos un callback booleano que gestione la respuesta
        RoundRepository.BooleanCallback booleanCallback = new RoundRepository.BooleanCallback() {
            /**
             * Gestiona la respuesta a la creación de una nueva partida
             * @param ok Boolean respuesta a cómo se ha ejecutado la función
             */
            @Override
            public void onResponse(boolean ok) {
                // Sacamos un Snackbar que muestre el resultado de la operación
                if (ok) {
                    Snackbar.make(roundRecyclerView,
                            R.string.repository_round_create_success, Snackbar.LENGTH_LONG).show();
                    // Si es correcto, también actualizamos la interfaz
                    updateUI();
                }
                else
                    Snackbar.make(roundRecyclerView,
                            R.string.repository_round_create_error, Snackbar.LENGTH_LONG).show();
            }
        };

        // Creamos una partida nueva y le colocamos los datos del jugador que la va a jugar
        Round round = new Round(Preferences.getSize(this.getContext()), this.type);
        if (this.type == Round.Type.LOCAL)
            round.setSecondUser(Preferences.getPlayerName(this.getContext()),
                    Preferences.getPlayerUUID(this.getContext()));
        else
            round.setFirstUser(Preferences.getPlayerName(this.getContext()),
                    Preferences.getPlayerUUID(this.getContext()));

        // Añadimos la partida al repositorio de datos y actualizamos la interfaz
        repository.addRound(round, booleanCallback);
    }
}
