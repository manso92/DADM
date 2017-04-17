package es.uam.eps.dadm.activities;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import es.uam.eps.dadm.R;
import es.uam.eps.dadm.model.RoundRepository;
import es.uam.eps.dadm.model.RoundRepositoryFactory;

/**
 * ScoresActivity es la actividad que mostrará la lista de usuarios disponibles en la base de datos
 * y las partidas que han jugado en la aplicación
 *
 * @author Pablo Manso
 * @version 12/04/2017
 */
public class ScoresActivity extends AppCompatActivity {

    /**
     * Instancia del recycler
     */
    RecyclerView roundRecyclerView;

    /**
     * Adapter que manejará nuestra lista personalizada
     */
    private RoundAdapter roundAdapter;


    /**
     * Crea todo lo necesario para la correcta ejecución de la actividad
     * @param savedInstanceState Pares clave valor que nos pasa la actividad que nos invoca
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Llamamos a la clase padre
        super.onCreate(savedInstanceState);
        // Cargamos el layout de la acitividad
        setContentView(R.layout.activity_scores);

        getSupportActionBar().setTitle(R.string.settings_scores);

        // Instanciamos el recycler view
        roundRecyclerView = (RecyclerView) findViewById(R.id.scores_recycler_view);
        // Cogemos el layout manager y le ajustamos un linearlayout para la lista
        RecyclerView.LayoutManager linearLayoutManager = new
                LinearLayoutManager(this);
        roundRecyclerView.setLayoutManager(linearLayoutManager);
        roundRecyclerView.setItemAnimator(new DefaultItemAnimator());

        // Actualizamos la interfaz y devolvemos la vista
        updateUI();
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
     * Actualiza la interfaz, la lista de usuarios y sus puntuaciones
     */
    public void updateUI() {
        // Instanciamos el repositorio de datos y creamos un callback para cuando pedimos la lista
        // de puntuaciones
        RoundRepository repository = RoundRepositoryFactory.createRepository(this);
        RoundRepository.ScoresCallback scoresCallback = new RoundRepository.ScoresCallback(){

            @Override
            public void onResponse(List<String> players, List<Integer> rounds) {
                if (roundAdapter == null) {
                    // Pasamos al adapter la lista de jugadores y puntuaciones y se lo colocamos al recyclerview
                    roundAdapter = new RoundAdapter(players, rounds);
                    roundRecyclerView.setAdapter(roundAdapter);
                }
                // Indicamos al roundadapter que los datos han cambiado
                else {
                    roundAdapter.setData(players, rounds);
                }
            }

            /**
             * Gestiona el error al consultar la lista de partidas del repositorio
             * @param error Mensaje de error que se mandará
             */
            @Override
            public void onError(String error) {
                // Mostramos el error que nos indican al llamar al callback
                Snackbar.make(findViewById(R.id.scores_recycler_view), error
                        , Snackbar.LENGTH_LONG).show();
            }
        };
        // Regcargamos la lista de puntuaciones disponibles
        repository.getScores(scoresCallback);

    }


    /**
     * Adaptador que se encargará de controlar la interfaz de la lista de usuarios y puntuaciones
     *
     * @author Pablo Manso
     * @version 12/04/2017
     */
    public class RoundAdapter extends RecyclerView.Adapter<RoundAdapter.RoundHolder> {
        /**
         * Lista de usuarios que se mostrará
         */
        private List<String> users;

        /**
         * Lista de puntuaciones que se mostrará
         */
        private List<Integer> scores;

        /**
         * Holder que se encargará de colocar en la interfaz cada uno de los elementos del item
         *
         * @author Pablo Manso
         * @version 12/04/2017
         */
        public class RoundHolder extends RecyclerView.ViewHolder {

            /**
             * Textview que contiene el nombre de usuario
             */
            private TextView userTextView;

            /**
             * Textview que mostrará la puntuación del usuario
             */
            private TextView scoreTextView;

            /**
             * Constructor del item en base a la vista que nos indican
             * @param itemView Item al que modificar las views
             */
            public RoundHolder(View itemView) {
                // Llamamos a la clase padre para la construcción
                super(itemView);
                // Guardamos un enlacea a cada una de las vistas a modificar más adelante
                userTextView = (TextView) itemView.findViewById(R.id.list_item_user);
                scoreTextView = (TextView) itemView.findViewById(R.id.list_item_score);
            }

            /**
             * Coloca los datos en la vista
             * @param user Usuario de la aplicación
             * @param score Puntuación del usuario
             */
            public void bindRound(String user, int score) {
                // Colocamos la información en los diferentes contenedores
                userTextView.setText(user);
                scoreTextView.setText(Integer.toString(score));
            }
        }

        /**
         * Constructor al que le pasamos los parámetros a mostrar
         * @param users Lista de usuarios disponibles
         * @param scores Puntuaciones de los usuarios
         */
        public RoundAdapter(List<String> users, List<Integer> scores) {
            this.users = users;
            this.scores = scores;
        }

        /**
         * Modifica los datos del adaptador y los muestra
         * @param users Lista de usuarios disponibles
         * @param scores Puntuaciones de los usuarios
         */
        public void setData (List<String> users, List<Integer> scores) {
            // Guardamos los datos
            this.users = users;
            this.scores = scores;
            // Actualizamos las vistas
            this.notifyDataSetChanged();
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
            View view = layoutInflater.inflate(R.layout.list_item_score, parent, false);
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
            holder.bindRound(users.get(position), scores.get(position));
        }

        /**
         * Nos indica el número de elementos que tiene la lista
         * @return Número de items que tiene la lista
         */
        @Override
        public int getItemCount() {
            return this.users.size();
        }
    }
}