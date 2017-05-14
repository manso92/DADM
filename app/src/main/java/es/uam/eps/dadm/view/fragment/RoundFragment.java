package es.uam.eps.dadm.view.fragment;

import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;

import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import es.uam.eps.dadm.R;
import es.uam.eps.dadm.events.NewMessageEvent;
import es.uam.eps.dadm.events.ShowMsgEvent;
import es.uam.eps.dadm.model.JugadorHumano;
import es.uam.eps.dadm.model.Preferences;
import es.uam.eps.dadm.model.Round;
import es.uam.eps.dadm.server.LocalServerPlayer;
import es.uam.eps.dadm.server.RemotePlayer;
import es.uam.eps.dadm.view.activities.Jarvis;
import es.uam.eps.dadm.view.views.TableroView;
import es.uam.eps.multij.*;

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

    /**
     * Instancia de Butterknife para hacer unbinding más adelante
     */
    Unbinder unbinder;

    /**
     * Vista del tablero
     */
    @BindView(R.id.board_view)
    TableroView boardView;

    /**
     * Título de la partida
     */
    @BindView(R.id.round_title)
    TextView roundTitle;

    /**
     * Botón para reiniciar la partida
     */
    @BindView(R.id.reset_round_fab)
    FloatingActionButton resetRoundFab;

    /**
     * Layout contenedor de toda la vista
     */
    @BindView(R.id.coordinatorRound)
    CoordinatorLayout coordinatorRound;

    /**
     * Ronda en juego
     */
    private Round round;

    /**
     * Constructor vacío del fragmento
     */
    public RoundFragment() {}

    /**
     * Devuelve una instancia del fragment con los datos cargados necesarios para arrancar
     * @param round Ronda que jugaremos en el fragment
     * @return Instnacia del fragment
     */
    public static RoundFragment newInstance(Round round) {
        // Creamos un contenedor para los argumentos
        Bundle args = new Bundle();
        // Creamos una instancia del fragmento, ponemos los argumentos y la devolvemos
        RoundFragment roundFragment = new RoundFragment();
        roundFragment.setArguments(round.roundToBundle(args));
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
        this.round = Round.bundleToRound(getArguments());

        // Si hay un error con la ronda, finalizamos la actividad
        if (this.round == null)
            this.getActivity().finish();

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
        // Cargamos el layout del fragmento y hacemos binding de los componentes
        final View rootView = inflater.inflate(R.layout.fragment_round, container, false);
        unbinder = ButterKnife.bind(this, rootView);

        // Cambiamos el título por el de la partida
        roundTitle.setText(this.round.getTitle());

        // Si no es una partida local, no podremos reiniciarla
        if (this.round.getTipo() != Round.Type.LOCAL)
            resetRoundFab.setVisibility(View.GONE);

        return rootView;
    }

    /**
     * Maneja la destrucción de la vista para hacer unbinding de los componentes
     */
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

        // Empezamos a capturar los eventos
        Jarvis.eventRegister(this);

        // Comenzamos la partida
        if (this.round.getTipo() == Round.Type.LOCAL)
            startLocalRound();
        else
            startServerRound();
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
     * Comienza una partida local
     */
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

    /**
     * Comienza una partida con el servidor
     */
    void startServerRound() {
        // Creamosdps jugadores, uno local y uno en servidor
        ArrayList<Jugador> players = new ArrayList<Jugador>();
        Jugador firstPlayer = null, secondPlayer = null;

        // Miramos quien es el jugador local y quien el servidor y los creamos
        if (this.round.turn(Preferences.getPlayerName(this.getContext())) == 1) {
            firstPlayer = new LocalServerPlayer(getContext(), round);
            secondPlayer = new RemotePlayer(round.getSecondUserName());
        } else if (this.round.turn(Preferences.getPlayerName(this.getContext())) == 2) {
            secondPlayer = new LocalServerPlayer(getContext(), round);
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
                // Actualizamos el tablero
                boardView.invalidate();
                break;
            // Evento de que hay un fin de partida
            case Evento.EVENTO_FIN:
                // Actualizamos el tablero
                boardView.invalidate();
                // Indicamos al jugador que la partida ha acabado
                Jarvis.error(ShowMsgEvent.Type.TOAST, R.string.game_game_over_title, getContext());
                this.getActivity().finish();
                break;
        }
    }

    /**
     * Captura las pulsaciones que se hacen en el fab para reiniciar la partida
     * @param view View del fab
     */
    @OnClick(R.id.reset_round_fab)
    public void onClick(View view) {
        // Si la partida ya ha acabado se lo indicamos al usuario
        if (round.getBoard().getEstado() != Tablero.EN_CURSO) {
            Jarvis.error(ShowMsgEvent.Type.TOAST, R.string.game_round_finished, getContext());
            return;
        }

        // Reiniciamos la vista
        boardView.reset();

        // Reiniciamos el tablero y comenzamos la partida
        round.getBoard().reset();
        startLocalRound();

        // Llamamos al callback de la actualización y le indicamos al jugador que el cambio de ha hecho
        Jarvis.error(ShowMsgEvent.Type.SNACKBAR, R.string.game_round_restarted, getContext());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(NewMessageEvent msg) throws ExcepcionJuego {
        Log.d(DEBUG, "Mensaje recibido en el roundgrafment: " + msg.toString());

        if ((msg.getMsgtype() == NewMessageEvent.newMovement) && (msg.getSender().equals(this.round.getId()))){
            this.round.getBoard().stringToTablero(msg.getContent());
            boardView.invalidate();
        }
    }
}