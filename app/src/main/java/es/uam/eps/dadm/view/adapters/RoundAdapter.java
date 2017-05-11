package es.uam.eps.dadm.view.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.uam.eps.dadm.R;
import es.uam.eps.dadm.model.Round;
import es.uam.eps.dadm.view.views.TableroView;

/**
 * @author Pablo Manso
 * @version 11/05/2017
 */

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


    public Round getRound(int position) {
        return this.rounds.get(position);
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
