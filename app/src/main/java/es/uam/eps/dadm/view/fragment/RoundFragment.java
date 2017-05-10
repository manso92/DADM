package es.uam.eps.dadm.view.fragment;

import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import es.uam.eps.dadm.R;
import es.uam.eps.dadm.model.JugadorHumano;
import es.uam.eps.dadm.model.Preferences;
import es.uam.eps.dadm.model.Round;
import es.uam.eps.dadm.server.LocalServerPlayer;
import es.uam.eps.dadm.server.RemotePlayer;
import es.uam.eps.dadm.view.views.TableroView;
import es.uam.eps.multij.Evento;
import es.uam.eps.multij.ExcepcionJuego;
import es.uam.eps.multij.Jugador;
import es.uam.eps.multij.JugadorAleatorio;
import es.uam.eps.multij.Partida;
import es.uam.eps.multij.PartidaListener;
import es.uam.eps.multij.Tablero;

/**
 * RoundFragment es un fragmento que mostrará el tablero de las damas con el que el jugador comenzará la partida
 *
 * @author Pablo Manso
 * @version 13/03/2017
 */
public class RoundFragment extends Fragment implements PartidaListener {

    /**
     * Tag para escribir en el log
     */
    public static final String DEBUG = "Damas.RoundFrag";

    public static final String ARG_ROUND_ID = "es.uam.eps.dadm.round_id";
    public static final String ARG_FIRST_PLAYER_NAME = "es.uam.eps.dadm.first_player_name";
    public static final String ARG_FIRST_PLAYER_UUID = "es.uam.eps.dadm.first_player_uuid";
    public static final String ARG_SECOND_PLAYER_NAME = "es.uam.eps.dadm.second_player_name";
    public static final String ARG_SECOND_PLAYER_UUID = "es.uam.eps.dadm.second_player_uuid";
    public static final String ARG_ROUND_TYPE = "es.uam.eps.dadm.round_type";
    public static final String ARG_ROUND_SIZE = "es.uam.eps.dadm.round_size";
    public static final String ARG_ROUND_DATE = "es.uam.eps.dadm.round_date";
    public static final String ARG_ROUND_BOARD = "es.uam.eps.dadm.round_board";


    Unbinder unbinder;
    @BindView(R.id.board_view)
    TableroView boardView;
    @BindView(R.id.round_title)
    TextView roundTitle;
    @BindView(R.id.reset_round_fab)
    FloatingActionButton resetRoundFab;
    @BindView(R.id.coordinatorRound)
    CoordinatorLayout coordinatorRound;

    /**
     * Ronda en juego
     */
    private Round round;


    /**
     * Constructor vacío del fragmento
     */
    public RoundFragment() {
    }

    public static RoundFragment newInstance(Round round) {
        // Creamos un contenedor para los argumentos
        Bundle args = new Bundle();

        // Ponemos uno a uno todos los argumentos en el contenedor
        args.putString(ARG_ROUND_ID, round.getId());
        args.putSerializable(ARG_ROUND_TYPE, round.getTipo());
        args.putString(ARG_FIRST_PLAYER_NAME, round.getFirstUserName());
        args.putString(ARG_FIRST_PLAYER_UUID, round.getFirstUserUUID());
        args.putString(ARG_SECOND_PLAYER_NAME, round.getSecondUserName());
        args.putString(ARG_SECOND_PLAYER_UUID, round.getSecondUserUUID());
        args.putString(ARG_ROUND_SIZE, Integer.toString(round.getSize()));
        args.putString(ARG_ROUND_DATE, round.getDate());
        args.putString(ARG_ROUND_BOARD, round.getBoard().tableroToString());

        // Creamos una instancia del fragmento, ponemos los argumentos y la devolvemos
        RoundFragment roundFragment = new RoundFragment();
        roundFragment.setArguments(args);
        return roundFragment;
    }

    private Round roundFromBundle() {
        int size = Integer.parseInt(getArguments().getString(ARG_ROUND_SIZE));
        Round round = new Round(getArguments().getString(ARG_ROUND_ID),
                (Round.Type) getArguments().getSerializable(ARG_ROUND_TYPE),
                getArguments().getString(ARG_ROUND_DATE),
                Integer.parseInt(getArguments().getString(ARG_ROUND_SIZE)));

        round.setFirstUser(getArguments().getString(ARG_FIRST_PLAYER_NAME),
                getArguments().getString(ARG_FIRST_PLAYER_UUID));
        round.setSecondUser(getArguments().getString(ARG_SECOND_PLAYER_NAME),
                getArguments().getString(ARG_SECOND_PLAYER_UUID));

        try {
            round.getBoard().stringToTablero(getArguments().getString(ARG_ROUND_BOARD));
        } catch (ExcepcionJuego excepcionJuego) {
            excepcionJuego.printStackTrace();
            this.getActivity().finish();
        }
        return round;
    }

    /**
     * Prepara el fragmento para su ejecución
     *
     * @param savedInstanceState Pares clave-valor con los datos que serán necesarios para la ejecución del fragmento
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Creamos una ronda y vamos metiendo uno a uno todos los argumentos
        this.round = this.roundFromBundle();
    }

    /**
     * Ejecutará las acciones necesarias para cuando se cree la vista
     *
     * @param inflater           Clase que se encargará de mostrar los elementos del fragment
     * @param container          Contenedor del fragment
     * @param savedInstanceState Pares clave valor que se nos dan como parámetro
     * @return View que se acaba de crear
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Cargamos el layout del fragmento y hacemos binding de los componentes
        final View rootView = inflater.inflate(R.layout.fragment_round, container, false);
        unbinder = ButterKnife.bind(this, rootView);

        // Cambiamos el título por el de la partida
        roundTitle.setText(this.round.getTitle());

        if (this.round.getTipo() != Round.Type.LOCAL)
            resetRoundFab.setVisibility(View.GONE);

        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }


    /**
     * Ejecución al inicio del fragmento
     */
    @Override
    public void onStart() {
        // Llamamos al padre
        super.onStart();
        // Comenzamos la partida
        if (this.round.getTipo() == Round.Type.LOCAL)
            startLocalRound();
        else
            startServerRound();
    }


    @OnClick(R.id.reset_round_fab)
    public void onClick(View view) {
        // Si la partida ya ha acabado se lo indicamos al usuario
        if (round.getBoard().getEstado() != Tablero.EN_CURSO) {
            Snackbar.make(coordinatorRound, R.string.game_round_finished, Snackbar.LENGTH_SHORT).show();
            return;
        }
        boardView.reset();
        // Reiniciamos el tablero y comenzamos la partida
        round.getBoard().reset();
        //
        // TODO QUE OSTIAS HACER CON ESTO
        // startRound();
        // Llamamos al callback de la actualización y le indicamos al jugador que el cambio de ha hecho
        Snackbar.make(coordinatorRound, R.string.game_round_restarted, Snackbar.LENGTH_SHORT).show();
    }

    void startLocalRound() {
        // Creamos un jugador aleatorio y uno local que manejará el juego y los metemos en una Lista
        ArrayList<Jugador> players = new ArrayList<Jugador>();

        JugadorAleatorio randomPlayer = new JugadorAleatorio("Random player");
        JugadorHumano localPlayer = new JugadorHumano(this.getContext(), round);
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
        this.boardView.setBoard(round.getBoard());
        this.boardView.setOnPlayListener(localPlayer);

        // Si la partida no ha comenzado, la empezamos
        if (game.getTablero().getEstado() == Tablero.EN_CURSO) game.comenzar();
    }

    void startServerRound() {
        // Creamosdps jugadores, uno local y uno en servidor
        ArrayList<Jugador> players = new ArrayList<Jugador>();
        Jugador firstPlayer = null, secondPlayer = null;

        if (this.round.turn(Preferences.getPlayerName(this.getContext())) == 1) {
            firstPlayer = new LocalServerPlayer(coordinatorRound, round);
            secondPlayer = new RemotePlayer(round.getSecondUserName());
        } else if (this.round.turn(Preferences.getPlayerName(this.getContext())) == 2) {
            secondPlayer = new LocalServerPlayer(coordinatorRound, round);
            firstPlayer = new RemotePlayer(round.getFirstUserName());
        } else {
            this.getActivity().finish();
        }

        // Añadimos los jugadores al arraylist
        players.add(firstPlayer);
        players.add(secondPlayer);

        // Creamos una partida con el tablero que nos han indicado y con los jugadores que hemos creado
        Partida game = new Partida(round.getBoard(), players);

        // Nos ponemos como observadores de modo que nos indiquen los cambios de la partida para
        // poder ponerlos en la interfaz gráfica
        game.addObservador(this);

        // Instanciamos el tablero de juego, le pasamos el tablero y el jugador que jugará la partida
        this.boardView.setBoard(round.getBoard());
        if (this.round.turn(Preferences.getPlayerName(this.getContext())) == 1) {
            ((LocalServerPlayer) firstPlayer).setPartida(game);
            this.boardView.setOnPlayListener(((LocalServerPlayer) firstPlayer));
        } else {
            ((LocalServerPlayer) secondPlayer).setPartida(game);
            this.boardView.setOnPlayListener(((LocalServerPlayer) secondPlayer));
        }

        // Si la partida no ha comenzado, la empezamos
        if (game.getTablero().getEstado() == Tablero.EN_CURSO) game.comenzar();
    }

    /**
     * Capturamos los eventos que se producen en la partida
     *
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
                break;
            // Evento de que hay un fin de partida
            case Evento.EVENTO_FIN:
                // Actualizamos el tablero y llamamos al callback que hay que llamar cuando se actualiza la ronda
                boardView.invalidate();
                // Indicamos al jugador que la partida ha acabado
                Snackbar.make(coordinatorRound, R.string.game_game_over_title, Snackbar.LENGTH_SHORT).show();
                this.getActivity().finish();
                break;
        }
    }
}