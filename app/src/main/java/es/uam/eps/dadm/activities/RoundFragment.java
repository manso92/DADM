package es.uam.eps.dadm.activities;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import es.uam.eps.dadm.R;
import es.uam.eps.dadm.model.Round;
import es.uam.eps.dadm.model.RoundRepository;
import es.uam.eps.dadm.views.TableroView;
import es.uam.eps.multij.*;

/**
 * RoundFragment es un fragmento que mostrará el tablero de las damas con el que el jugador comenzará la partida
 * @author Pablo Manso
 * @version 13/03/2017
 */
public class RoundFragment extends Fragment implements PartidaListener{
    /**
     * String que contiene la clave del par clave-valor que nos indicará la partida que se va a jugar
     */
    public static final String ARG_ROUND_ID = "es.uam.eps.dadm.round_id";

    /**
     * Partida que jugará el usuario
     */
    private Round round;

    /**
     * View que dibujará el tablero de las damas en el fragmento
     */
    private TableroView boardView;

    /**
     * Callback que se ejecutará cada vez que se hace una modificación en la partida
     */
    private Callbacks callbacks;

    /**
     * Interfaz que se implementará para ser llamado cada vez que se actualice la partida
     */
    public interface Callbacks {
        void onRoundUpdated(Round round);
    }

    /**
     * Constructor vacío del fragmento
     */
    public RoundFragment() {}

    /**
     * Crea una instancia del fragmento de modo que se le indica la partida que se jugará
     * @param roundId Id de la partida que se va a jugar
     * @return Instancia del fragmento que se acaba de crear
     */
    public static RoundFragment newInstance(String roundId) {
        // Creamos un Bundle y ponemos el id de la partida en los argumentos
        Bundle args = new Bundle();
        args.putString(ARG_ROUND_ID, roundId);
        // Creamos una instancia del fragmento y le adjuntamos los argumentos del bundle que acabamos de crear
        RoundFragment roundFragment = new RoundFragment();
        roundFragment.setArguments(args);
        // Devolvemos el fragmento
        return roundFragment;
    }

    /**
     * Prepara el fragmento para su ejecución
     * @param savedInstanceState Pares clave-valor con los datos que serán necesarios para la ejecución del fragmento
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        // Llamamos a la clase padre
        super.onCreate(savedInstanceState);
        // Obtenemos el id de la partida ya la buscamos en el repositorio de partidas
        String roundId = getArguments().getString(ARG_ROUND_ID);
        round = RoundRepository.get(getActivity()).getRound(roundId);
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
        final View rootView = inflater.inflate(R.layout.fragment_round, container, false);
        // Obtenemos el textview del título y ponemos el valor de la partida
        TextView roundTitleTextView = (TextView) rootView.findViewById(R.id.round_title);
        roundTitleTextView.setText(round.getTitle());
        return rootView;
    }

    /**
     * Ejecución al inicio del fragmento
     */
    @Override
    public void onStart() {
        // Llamamos al padre
        super.onStart();
        // Comenzamos la partida
        startRound();
    }

    /**
     * Buscamos los elementos que queramos escuchar y les asociamos el listener que maneje sus eventos
     */
    private void registerListeners() {
        // Buscamos el botón flotante típico de Android 5.0...
        FloatingActionButton resetButton = (FloatingActionButton)
                getView().findViewById(R.id.reset_round_fab);
        // ... y le añadimos un listener
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Si la partida ya ha acabado se lo indicamos al usuario
                if (round.getBoard().getEstado() != Tablero.EN_CURSO) {
                    Snackbar.make(getView(), R.string.round_already_finished,
                            Snackbar.LENGTH_SHORT).show();
                    return;
                }
                boardView.reset();
                // Reiniciamos el tablero y comenzamos la partida
                round.getBoard().reset();
                startRound();
                // Llamamos al callback de la actualización y le indicamos al jugador que el cambio de ha hecho
                callbacks.onRoundUpdated(round);
                Snackbar.make(getView(), R.string.round_restarted,
                        Snackbar.LENGTH_SHORT).show();
            }
        });
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
     * Comienza una nueva partida a través de lo que nos ha indicado la actividad que nos invocó
     */
    void startRound() {
        // Creamos un jugador aleatorio y uno local que manejará el juego y los metemos en una Lista
        ArrayList<Jugador> players = new ArrayList<Jugador>();
        JugadorAleatorio randomPlayer = new JugadorAleatorio("Random player");
        JugadorHumano localPlayer = new JugadorHumano();
        players.add(randomPlayer);
        players.add(localPlayer);

        // Creamos una partida con el tablero que nos han indicado y con los jugadores que hemos creado
        Partida game = new Partida(round.getBoard(), players);

        // Nos ponemos como observadores de modo que nos indiquen los cambios de la partida para
        // poder ponerlos en la interfaz gráfica
        game.addObservador(this);

        // Le decimos a la clase jugador cual es la partida que vamos a jugar
        localPlayer.setPartida(game);

        // Instanciamos el tablero de juego, le pasamos el tablero y el jugador que jugará la partida
        boardView = (TableroView) getView().findViewById(R.id.board_erview);
        boardView.setBoard(round.getBoard());
        boardView.setOnPlayListener(localPlayer);

        // Registramos los listeners de este fragmento
        registerListeners();

        // Si la partida no ha comenzado, la empezamos
        if (game.getTablero().getEstado() == Tablero.EN_CURSO) game.comenzar();
    }

    /**
     * Capturamos los eventos que se producen en la partida
     * @param evento Evento del juego al que tenemos que preparar una salida
     */
    @Override
    public void onCambioEnPartida(Evento evento) {
        // Miramos que tipo de evento nos envían y respondemos en base a ello
        switch (evento.getTipo()) {
            // Evento de que hay un cambio de de turno
            case Evento.EVENTO_CAMBIO:
                // Actualizamos el tablero y llamamos al callback que hay que llamar cuando se actualiza la ronda
                boardView.invalidate();
                callbacks.onRoundUpdated(round);
                break;
            // Evento de que hay un fin de partida
            case Evento.EVENTO_FIN:
                // Actualizamos el tablero y llamamos al callback que hay que llamar cuando se actualiza la ronda
                boardView.invalidate();
                callbacks.onRoundUpdated(round);
                // Indicamos al jugador que la partida ha acabado
                Snackbar.make(getView(), R.string.game_over, Snackbar.LENGTH_SHORT).show();
                new AlertDialogFragment().show(getActivity().getSupportFragmentManager(),
                        "ALERT DIALOG");
                break;
        }
    }
}