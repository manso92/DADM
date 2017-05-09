package es.uam.eps.dadm.view.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.BindView;
import butterknife.OnClick;
import butterknife.Unbinder;
import es.uam.eps.dadm.R;
import es.uam.eps.dadm.model.Round;
import es.uam.eps.dadm.model.RoundRepository;
import es.uam.eps.dadm.model.RoundRepositoryFactory;
import es.uam.eps.dadm.server.ServerInterface;
import es.uam.eps.dadm.server.ServerRepository;
import es.uam.eps.dadm.view.activities.PreferenceActivity;
import es.uam.eps.dadm.view.listeners.RecyclerItemClickListener;
import es.uam.eps.dadm.view.views.TableroView;

/**
 * RoundListFragment es el fragmento que mostrará la lista de partidas necesarias
 *
 * @author Pablo Manso
 * @version 13/03/2017
 */
public class RoundListFragment extends Fragment {

    /**
     * Instancia del recycler
     */
    @BindView(R.id.round_recycler_view)
    RecyclerView roundRecyclerView;

    /**
     * Instancia del botón de añadir partida
     */
    @BindView(R.id.add_found_fab)
    FloatingActionButton addFoundFab;

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
     * @param repository Repositorio de datos para el framento
     * @return Fragment con los parámetros añadidos
     */
    public static RoundListFragment newInstance(RoundRepository repository, Round.Type type) {
        // Creamos un fragmento y un bundle para los argumentos
        RoundListFragment fragment = new RoundListFragment();
        Bundle bundle = new Bundle();
        // Añadimos el repositorio, colocamos los valores y devolvemos el fragmento
        bundle.putSerializable(REPOSITORY_KEY, repository);
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

        // Obtenemos el repositorio, si no hay, pues lo creamos
        if ((getArguments() != null) && (getArguments().containsKey(REPOSITORY_KEY)))
            this.repository = (RoundRepository) getArguments().getSerializable(REPOSITORY_KEY);
        if (this.repository == null)
            this.repository = RoundRepositoryFactory.createRepository(getActivity());

        // Obtenemos el tipo de partidas que queremos
        if ((getArguments() != null) && (getArguments().containsKey(ROUNDTYPE_KEY)))
            this.type = (Round.Type) getArguments().getSerializable(ROUNDTYPE_KEY);
        else
            this.type = this.repository.getDefaultFilter();
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
        final View view = inflater.inflate(R.layout.fragment_round_list, container, false);
        this.unbinder = ButterKnife.bind(this, view);

        // Configuramos el recycler view y devolvemos la vista
        this.setupRecyclerView();

        // Miramos el tipo y ocultamos el botón si es necesario
        if ((this.type == Round.Type.OPEN) || (this.type == Round.Type.FINISHED))
            addFoundFab.setVisibility(View.GONE);

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
                        RoundRepository.RoundsCallback roundsCallback = new RoundRepository.RoundsCallback() {
                            @Override
                            public void onResponse(List<Round> rounds) {
                                // Llamamos al callback con la ronda que se ha seleccionado
                                callbacks.onRoundSelected(rounds.get(position), type);
                            }
                            @Override
                            public void onError(String error) {
                                // Mostramos el error que se ha producido al seleccionar el item
                                Snackbar.make(roundRecyclerView, R.string.repository_round_not_founded,
                                        Snackbar.LENGTH_LONG).show();
                            }
                        };
                        String playeruuid = PreferenceActivity.getPlayerUUID(getActivity());
                        repository.getRounds(playeruuid, null, type, roundsCallback);
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
                roundRecyclerView.setAdapter(roundAdapter);
            }
            @Override
            public void onError(String error) {
                // Mostramos el error que nos indican al llamar al callback
                Snackbar.make(roundRecyclerView, error, Snackbar.LENGTH_LONG).show();
            }
        };
        // Regcargamos la lista de rondas disponibles
        repository.getRounds(PreferenceActivity.getPlayerUUID(this.getActivity()),
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
     *  el evento click en el botón de añadir ronda
     * @param v View del botón que se pulsa
     */
    @OnClick(R.id.add_found_fab)
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
        Round round = new Round(PreferenceActivity.getSize(this.getContext()), this.type);
        if (this.type == Round.Type.LOCAL)
            round.setSecondUser(PreferenceActivity.getPlayerName(this.getContext()),
                    PreferenceActivity.getPlayerUUID(this.getContext()));
        else
            round.setFirstUser(PreferenceActivity.getPlayerName(this.getContext()),
                    PreferenceActivity.getPlayerUUID(this.getContext()));

        // Añadimos la partida al repositorio de datos y actualizamos la interfaz
        repository.addRound(round, booleanCallback);
    }


    /**
     * Adaptador que se encargará de controlar la interfaz de la lista de partidas
     *
     * @author Pablo Manso
     * @version 13/03/2017
     */
    public class RoundAdapter extends RecyclerView.Adapter<RoundAdapter.RoundHolder> {

        /**
         * Lista de partidas que se vana mostrar
         */
        private List<Round> rounds;

        /**
         * Holder que se encargará de colocar en la interfaz cada uno de los elementos del item
         *
         * @author Pablo Manso
         * @version 13/03/2017
         */
        public class RoundHolder extends RecyclerView.ViewHolder {

            /**
             * Textview que contiene el nombre de la partida
             */
            @BindView(R.id.list_item_id)
            TextView idTextView;

            /**
             * Textview que contiene una representación del tablero
             */
            @BindView(R.id.tableroViewThumb)
            TableroView tableroView;

            /**
             * Textview que mostrará la fecha de la partida
             */
            @BindView(R.id.list_item_date)
            TextView dateTextView;

            /**
             * Partida que representa la partida que queremos indicar en el item
             */
            private Round round;

            /**
             * Constructor del item en base a la vista que nos indican
             * @param itemView Item al que modificar las views
             */
            public RoundHolder(View itemView) {
                // Llamamos a la clase padre para la construcción y hacemos binding de componentes
                super(itemView);
                ButterKnife.bind(this, itemView);
            }

            /**
             * Ajusta los datos necesarios para mostrar en la vista
             * @param round Ronda que contiene los datos a mostrar
             */
            public void bindRound(Round round) {
                // Guardamos la ronda
                this.round = round;
                // Mostrarmos en los TextView la info de la partida
                idTextView.setText(round.getTitle());
                tableroView.setBoard(round.getBoard());
                dateTextView.setText(String.valueOf(round.getDate()).substring(0, 19));
            }
        }

        /**
         * Constructor del adaptador
         * @param rounds Lista de rondas a mostrar
         */
        public RoundAdapter(List<Round> rounds) {
            this.rounds = rounds;
        }

        /**
         * Cambia la lista de rondas que tiene actualmente el adapter
         * @param rounds Lista de partidas a cargar
         */
        public void setRounds(List<Round> rounds) {
            this.rounds = rounds;
            this.notifyDataSetChanged();
        }

        /**
         * Añade una lista de partidas a la lista de partidas actual
         * @param rounds Lista de partidas a añadir
         */
        public void addRounds(List<Round> rounds) {
            if (this.rounds == null) this.rounds = rounds;
            else this.rounds.addAll(rounds);
            this.notifyDataSetChanged();
        }

        /**
         * Limpia la lista de partidas del adapter
         */
        public void clear() {
            this.rounds = new ArrayList<Round>();
        }

        /**
         * Función que se ejecutará cuando se cree la vista de cada item
         * @param parent Vista que contendrá la lista
         * @param viewType Tipo de vista
         * @return Holder que acabamos de crear
         */
        @Override
        public RoundHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            // Cogemos el inflater para poder colocar nuestra vista
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            // Creamos la vista cargando el layout del item necesario
            View view = layoutInflater.inflate(R.layout.list_item_round, parent, false);
            // Retornamos el objeto que manejará la vista
            return new RoundHolder(view);
        }

        /**
         * Función que irá colocando los datos necesarios en las vistas que acabamos de crear
         * @param holder Contenedor que hay que rellenar
         * @param position Posición de la lista que hay que rellenar
         */
        @Override
        public void onBindViewHolder(RoundHolder holder, int position) {
            // Obtenemos la info de la partida que vamos a cargar
            Round round = rounds.get(position);
            // Rellenamos  los datos con la ronda que acabamos de obtener
            holder.bindRound(round);
        }

        /**
         * Nos indica el número de elementos que tiene la lista
         * @return Número de items que tiene la lista
         */
        @Override
        public int getItemCount() {
            return rounds.size();
        }
    }
}
