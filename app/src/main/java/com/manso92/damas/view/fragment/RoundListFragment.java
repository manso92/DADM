package com.manso92.damas.view.fragment;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;

import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.BindView;
import butterknife.OnClick;
import butterknife.Unbinder;
import com.manso92.damas.R;
import com.manso92.damas.events.NewMessageEvent;
import com.manso92.damas.events.RefreshRoundListEvent;
import com.manso92.damas.events.ShowMsgEvent;
import com.manso92.damas.model.Round;
import com.manso92.damas.model.RoundRepository;
import com.manso92.damas.model.RoundRepositoryFactory;
import com.manso92.damas.model.Preferences;
import com.manso92.damas.view.activities.Jarvis;
import com.manso92.damas.view.adapters.RoundAdapter;
import com.manso92.damas.view.listeners.RecyclerItemClickListener;
import es.uam.eps.multij.ExcepcionJuego;

/**
 * RoundListFragment es el fragmento que mostrará la lista de partidas necesarias
 *
 * @author Pablo Manso
 * @version 13/05/2017
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
     * Número de columnas que tendrá nuestra aplicación
     */
    private int columns;

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

        // Configuramos el número de columnas que tendrá nuestra vista
        this.columns = getContext().getResources().getInteger(R.integer.gridlayoutItemCount);
    }

    /**
     * Ejecución al inicio del fragmento
     */
    @Override
    public void onStart() {
        // Llamamos al padre
        super.onStart();

        // Empezamos a capturar los eventos
        Jarvis.eventRegister(this);
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
     * Ejecución con el fin del fragmento
     */
    @Override
    public void onStop() {
        super.onStop();

        // Dejamos de campturar eventos
        Jarvis.eventUnregister(this);
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
        // Cogemos el layout manager y le ajustamos un gridlayout para la lista
        this.roundRecyclerView.setLayoutManager(new GridLayoutManager(this.getContext(),columns));
        this.roundRecyclerView.addItemDecoration(new GridSpacingItemDecoration(columns, 10, true));
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
                    roundAdapter = new RoundAdapter(rounds, getContext());
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
                Jarvis.error(ShowMsgEvent.Type.TOAST, error);
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
                    Jarvis.error(ShowMsgEvent.Type.SNACKBAR,
                            R.string.repository_round_create_success, getContext());
                    // Si es correcto, también actualizamos la interfaz
                    updateUI();
                }
                else
                    Jarvis.error(ShowMsgEvent.Type.TOAST,
                            R.string.repository_round_create_error, getContext());
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

    /**
     * Clase que gestionará como de muestra el recyclerview de las partidas
     */
    private class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

        /**
         * Número de columnas
         */
        private int spanCount;

        /**
         * Espaciado entre columnas
         */
        private int spacing;

        /**
         * Si se debe incluir o no spacing en los bordes
         */
        private boolean includeEdge;

        /**
         * Constructor del GridSpacingItemDecoration
         * @param spanCount Columnas
         * @param spacing Spacing entre ellas
         * @param includeEdge si debe haber bordes o no
         */
        public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
            this.spanCount = spanCount;
            this.spacing = Jarvis.dpToPx(spacing,getContext());
            this.includeEdge = includeEdge;
        }

        /**
         * Define el offset de cada elemento del recyclerview
         * @param outRect Rectángulo que lo contendrá
         * @param view View que se va a mostrar
         * @param parent Recycler view que nos contiene
         * @param state Cosa que no sé para que es
         */
        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            // Cogemos la posición del elemento que es, y de ahí su posición en la columna
            int position = parent.getChildAdapterPosition(view);
            int column = position % spanCount;

            // Calculamos los márgenes dependiendo de los parámetros
            if (includeEdge) {
                // Calculamos los márgenes izquierdo y derecho
                outRect.left = spacing - column * spacing / spanCount;
                outRect.right = (column + 1) * spacing / spanCount;

                // Si es la primera fila le ponemos margen superior
                if (position < spanCount)
                    outRect.top = spacing;

                outRect.bottom = spacing;
            } else {
                // Calculamos los márgenes izquierdo y derecho
                outRect.left = column * spacing / spanCount;
                outRect.right = spacing - (column + 1) * spacing / spanCount;

                // Solo ponemos margen superior a las filas que no sean la primera
                if (position >= spanCount)
                    outRect.top = spacing;

            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(RefreshRoundListEvent msg) {
        Log.d(DEBUG, "Mensaje recibido en el roundgrafment: " + msg.toString());

        if (msg.getTipo() == this.type){
            this.updateUI();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(NewMessageEvent msg) throws ExcepcionJuego{
        Log.d(DEBUG, "Mensaje recibido en el roundgrafment: " + msg.toString());

        if ((msg.getMsgtype() == NewMessageEvent.newMovement) && (this.roundAdapter.existsID(msg.getSender()))){
            this.roundAdapter.getRound(msg.getSender()).getBoard().stringToTablero(msg.getContent());
            this.roundAdapter.ordena();
        }
    }
}
