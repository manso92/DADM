package es.uam.eps.dadm.view.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.uam.eps.dadm.R;
import es.uam.eps.dadm.model.Round;
import es.uam.eps.dadm.model.RoundRepository;
import es.uam.eps.dadm.model.RoundRepositoryFactory;
import es.uam.eps.dadm.view.activities.LoginActivity;
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
    private RecyclerView roundRecyclerView;

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
        void onRoundSelected(Round round);
    }

    /**
     * Crea todo lo necesario para la correcta ejecución del fragmento
     * @param savedInstanceState Pares clave valor que nos pasa la actividad que nos invoca
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        // Llamamos al constructor
        super.onCreate(savedInstanceState);
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
        // Cargamos el layout del fragmento
        final View view = inflater.inflate(R.layout.fragment_round_list, container, false);
        // Cogemos el recycler view de la vista
        roundRecyclerView = (RecyclerView) view.findViewById(R.id.round_recycler_view);
        // Cogemos el layout manager y le ajustamos un linearlayout para la lista
        RecyclerView.LayoutManager linearLayoutManager = new
                LinearLayoutManager(getActivity());
        roundRecyclerView.setLayoutManager(linearLayoutManager);
        roundRecyclerView.setItemAnimator(new DefaultItemAnimator());

        // Añadimos el listener de la lista
        roundRecyclerView.addOnItemTouchListener(new
                RecyclerItemClickListener(getActivity(), new
                RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, final int position) {
                        RoundRepository repository = RoundRepositoryFactory.createRepository(getActivity(),false);
                        RoundRepository.RoundsCallback roundsCallback = new RoundRepository.RoundsCallback() {
                            /**
                             * Gestiona la lista de partidas del repositorio
                             * @param rounds Lista de partidas
                             */
                            @Override
                            public void onResponse(List<Round> rounds) {
                                callbacks.onRoundSelected(rounds.get(position));
                            }

                            /**
                             * Gestiona el error al consultar la lista de partidas del repositorio
                             * @param error Mensaje de error que se mandará
                             */
                            @Override
                            public void onError(String error) {
                                Snackbar.make(getView(), R.string.repository_round_not_founded,
                                        Snackbar.LENGTH_LONG).show();
                            }
                        };
                        String playeruuid = PreferenceActivity.getPlayerUUID(getActivity());
                        repository.getRounds(playeruuid, null, null, roundsCallback);
                    }
                }));
        // Actualizamos la interfaz y devolvemos la vista
        updateUI();
        return view;
    }

    /**
     * Función que se ejecutará cuando se vuelva de una pausa
     */
    @Override
    public void onResume() {
        // Indicamos a la clase padre que hemos vuelto de la pausa y actualizamos la interfaz
        super.onResume();
        updateUI();
    }

    /**
     * Actualiza la interfaz, la lista de las partidas disponibles
     */
    public void updateUI() {
        // Instanciamos el repositorio de datos y creamos un callback para cuando pedimos la lista
        // de partidas disponibles
        RoundRepository repository = RoundRepositoryFactory.createRepository(this.getActivity(),false);
        RoundRepository.RoundsCallback roundsCallback = new RoundRepository.RoundsCallback(){
            @Override
            public void onResponse(List<Round> rounds) {
                roundAdapter = new RoundAdapter(rounds);
                roundRecyclerView.setAdapter(roundAdapter);
                /*if (roundAdapter == null) {
                    // Pasamos al adapter la lista de rondas y se lo colocamos al recyclerview
                    roundAdapter = new RoundAdapter(rounds);
                    roundRecyclerView.setAdapter(roundAdapter);
                }
                // Indicamos al roundadapter que los datos han cambiado
                else {
                    roundAdapter.setRounds(rounds);
                    roundAdapter.notifyDataSetChanged();
                }*/
            }
            @Override
            public void onError(String error) {
                // Mostramos el error que nos indican al llamar al callback
                Snackbar.make(getActivity().findViewById(R.id.viewpager), error
                        , Snackbar.LENGTH_LONG).show();
            }
        };
        // Regcargamos la lista de rondas disponibles
        repository.getRounds(PreferenceActivity.getPlayerUUID(this.getActivity()),
                             null,null,roundsCallback);

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


        public void setRounds(List<Round> rounds) {
            this.rounds = rounds;
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
