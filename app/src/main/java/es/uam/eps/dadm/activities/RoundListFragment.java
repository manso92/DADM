package es.uam.eps.dadm.activities;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import es.uam.eps.dadm.R;
import es.uam.eps.dadm.model.Round;
import es.uam.eps.dadm.model.RoundRepository;

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
    RecyclerView roundRecyclerView;

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
        // Indicamos que hay un menú que mostrar
        setHasOptionsMenu(true);
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
                    public void onItemClick(View view, int position) {
                        // Obtenemos la ronda en base a lo que el usuario ha indicado
                        Round round =
                                RoundRepository.get(getContext()).getRounds().get(position);
                        // Indicamos al callback que se ha seleccionado una partida
                        callbacks.onRoundSelected(round);
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
        if (roundAdapter == null) {
            // Cogemos la lista de las rondas que están disponibles
            List<Round> rounds = RoundRepository.get(this.getActivity()).getRounds();
            // Pasamos al adapter la lista de rondas y se lo colocamos al recyclerview
            roundAdapter = new RoundAdapter(rounds);
            roundRecyclerView.setAdapter(roundAdapter);
        }
        // Indicamos al roundadapter que los datos han cambiado
        else
            roundAdapter.notifyDataSetChanged();

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
     * Crea el menú con las opciones
     * @param menu Menú que se va a crear
     * @param inflater Clase que construirá nuestro menú
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Indicamos a la clase padre que se va a crear un menú
        super.onCreateOptionsMenu(menu, inflater);
        // Indicamos al inflater el menú que tiene que crear
        inflater.inflate(R.menu.menu, menu);
    }

    /**
     * Listener que saltará si se selecciona una opción del menú
     * @param item Item que se ha pulsado
     * @return Si se ha podido ejecutar la opción
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Cogemos la opción que se ha pulaado
        switch (item.getItemId()) {
            // Si se creauna nueva rnda
            case R.id.menu_item_new_round:
                // Creamos una partida, se la mandamos al repositorio y actualizamos la interfaz
                Round round = new Round(RoundRepository.SIZE);
                RoundRepository.get(getActivity()).addRound(round);
                updateUI();

                return true;
            // Por defecto indicamos a la clase padre que se ha seleccionado algo del menú
            default:
                return super.onOptionsItemSelected(item);
        }
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
            private TextView idTextView;
            /**
             * Textview que contiene una representación del tablero
             */
            private TextView boardTextView;
            /**
             * Textview que mostrará la fecha de la partida
             */
            private TextView dateTextView;
            /**
             * Partida que representa la partida que queremos indicar en el item
             */
            private Round round;

            /**
             * Constructor del item en base a la vista que nos indican
             * @param itemView Item al que modificar las views
             */
            public RoundHolder(View itemView) {
                // Llamamos a la clase padre para la construcción
                super(itemView);
                // Guardamos un enlacea a cada una de las vistas a modificar más adelante
                idTextView = (TextView) itemView.findViewById(R.id.list_item_id);
                boardTextView = (TextView) itemView.findViewById(R.id.list_item_board);
                dateTextView = (TextView) itemView.findViewById(R.id.list_item_date);
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
                boardTextView.setText(round.getBoard().toString());
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
