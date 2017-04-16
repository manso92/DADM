package es.uam.eps.dadm.activities;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import es.uam.eps.dadm.R;
import es.uam.eps.dadm.model.Round;
import es.uam.eps.dadm.model.RoundRepository;
import es.uam.eps.dadm.model.RoundRepositoryFactory;
import es.uam.eps.dadm.views.TableroView;
import es.uam.eps.multij.*;

/**
 * RoundFragment es un fragmento que mostrará el tablero de las damas con el que el jugador comenzará la partida
 * @author Pablo Manso
 * @version 13/03/2017
 */
public class RoundFragment extends Fragment implements PartidaListener {

    /**
     * Tag para escribir en el log
     */
    public static final String DEBUG = "DEBUG";

    /**
     * Id del argumento de la ronda
     */
    public static final String ARG_ROUND_ID = "es.uam.eps.dadm.round_id";

    /**
     * Id del argumento del nombre del jugador
     */
    public static final String ARG_FIRST_PLAYER_NAME = "es.uam.eps.dadm.player_name";

    /**
     * Id del argumento del nombre del jugador
     */
    public static final String ARG_FIRST_PLAYER_UUID = "es.uam.eps.dadm.player_uuid";

    /**
     * Id del argumento del título de la partida
     */
    public static final String ARG_ROUND_TITLE = "es.uam.eps.dadm.round_title";

    /**
     * Id del argumento del tamaño de la partida
     */
    public static final String ARG_ROUND_SIZE = "es.uam.eps.dadm.round_size";

    /**
     * Id del argumento de la fecha de la partida
     */
    public static final String ARG_ROUND_DATE = "es.uam.eps.dadm.round_date";

    /**
     * Id del argumento del tablero
     */
    public static final String ARG_ROUND_BOARD = "es.uam.eps.dadm.round_board";

    /**
     * Ronda en juego
     */
    private Round round;

    /**
     * View del tablero a actualizar con cada movimiento
     */
    private TableroView boardView;

    /**
     * Calback registrado para cuando se actualice la ronda
     */
    private Callbacks callbacks;

    /**
     * Interfaz que implementarán al que haya que notificar de que se ha actualizado la ronda
     */
    public interface Callbacks {
        void onRoundUpdated();
    }

    /**
     * Constructor vacío del fragmento
     */
    public RoundFragment() {}

    /**
     * Crea una instancia del fragmento de modo que se le indica la partida que se jugará
     * @param roundId Id de la partida que se va a jugar
     * @param playerName Nombre del jugador que la jugará
     * @param playerUUID iDENTIFICADOR DEL JUGADOR QUE LA JUGARÁ
     * @param roundTitle Título de la partida que se va a jugar
     * @param roundSize Tamaño del tablero de la partida
     * @param roundDate Fecha de la partida que se va a jugar
     * @param roundBoard Tablero que vamos a jugar
     * @return Instancia del fragmento que se acaba de crear
     */
    public static RoundFragment newInstance(String roundId, String playerName, String playerUUID,
                                            String roundTitle, int roundSize,
                                            String roundDate, String roundBoard) {
        // Creamos un contenedor para los argumentos
        Bundle args = new Bundle();

        // Ponemos uno a uno todos los argumentos en el contenedor
        args.putString(ARG_ROUND_ID, roundId);
        args.putString(ARG_FIRST_PLAYER_NAME, playerName);
        args.putString(ARG_FIRST_PLAYER_UUID, playerUUID);
        args.putString(ARG_ROUND_TITLE, roundTitle);
        args.putString(ARG_ROUND_SIZE, Integer.toString(roundSize));
        args.putString(ARG_ROUND_DATE, roundDate);
        args.putString(ARG_ROUND_BOARD, roundBoard);

        // Creamos una instancia del fragmento, ponemos los argumentos y la devolvemos
        RoundFragment roundFragment = new RoundFragment();
        roundFragment.setArguments(args);
        return roundFragment;
    }

    /**
     * Prepara el fragmento para su ejecución
     * @param savedInstanceState Pares clave-valor con los datos que serán necesarios para la ejecución del fragmento
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Creamos una ronda y vamos metiendo uno a uno todos los argumentos
        this.round = new Round(Integer.parseInt(getArguments().getString(ARG_ROUND_SIZE)));
        if (getArguments().containsKey(ARG_ROUND_ID))
            this.round.setId(getArguments().getString(ARG_ROUND_ID));
        if (getArguments().containsKey(ARG_FIRST_PLAYER_NAME))
            this.round.setPlayerName(getArguments().getString(ARG_FIRST_PLAYER_NAME));
        if (getArguments().containsKey(ARG_FIRST_PLAYER_UUID))
            this.round.setPlayerUUID(getArguments().getString(ARG_FIRST_PLAYER_UUID));
        if (getArguments().containsKey(ARG_ROUND_TITLE))
            this.round.setTitle(getArguments().getString(ARG_ROUND_TITLE));
        if (getArguments().containsKey(ARG_ROUND_DATE))
            this.round.setDate(getArguments().getString(ARG_ROUND_DATE));
        if (getArguments().containsKey(ARG_ROUND_BOARD))
            try {
                this.round.getBoard().stringToTablero(getArguments().getString(ARG_ROUND_BOARD));
            } catch (ExcepcionJuego excepcionJuego) {
                excepcionJuego.printStackTrace();
                this.getActivity().finish();
            }
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
        roundTitleTextView.setText(this.round.getTitle());
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
                    Snackbar.make(getView(), R.string.game_round_finished,
                            Snackbar.LENGTH_SHORT).show();
                    return;
                }
                boardView.reset();
                // Reiniciamos el tablero y comenzamos la partida
                round.getBoard().reset();
                startRound();
                // Llamamos al callback de la actualización y le indicamos al jugador que el cambio de ha hecho
                callbacks.onRoundUpdated();
                Snackbar.make(getView(), R.string.game_round_restarted,
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
                callbacks.onRoundUpdated();
                break;
            // Evento de que hay un fin de partida
            case Evento.EVENTO_FIN:
                // Actualizamos el tablero y llamamos al callback que hay que llamar cuando se actualiza la ronda
                boardView.invalidate();
                callbacks.onRoundUpdated();
                // Indicamos al jugador que la partida ha acabado
                Snackbar.make(getView(), R.string.game_game_over, Snackbar.LENGTH_SHORT).show();
                new AlertDialogFragment().show(getActivity().getSupportFragmentManager(),
                        "ALERT DIALOG");
                break;
        }
        // Actualizamos la partida en el repositorio
        updateRound();
    }

    /**
     * Actualiza la partida en el repositorio
     */
    private void updateRound() {
        // Obtenemos una referencia al repositorio y creamos un booleancalback
        RoundRepository repository = RoundRepositoryFactory.createRepository(getActivity());
        RoundRepository.BooleanCallback callback = new RoundRepository.BooleanCallback() {
            /**
             * Gestiona la respuesta del servidor a un evento de respuesta booelana
             * @param response Correcta ejecución de la función en el servidor
             */
            @Override
            public void onResponse(boolean response) {
                // Si se produce un error al actualizar la partida, se lo comunicamos al usuario
                if (response == false)
                    Snackbar.make(getView(), R.string.repository_round_error_updating,
                            Snackbar.LENGTH_LONG).show();
            }
        };
        // Actualizamos la partida en la base de datos
        repository.updateRound(round, callback);
    }

}