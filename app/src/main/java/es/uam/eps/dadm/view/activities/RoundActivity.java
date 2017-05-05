package es.uam.eps.dadm.view.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import es.uam.eps.dadm.R;
import es.uam.eps.dadm.model.JugadorHumano;
import es.uam.eps.dadm.model.Round;
import es.uam.eps.dadm.model.RoundRepository;
import es.uam.eps.dadm.model.RoundRepositoryFactory;
import es.uam.eps.dadm.view.fragment.AlertDialogFragment;
import es.uam.eps.dadm.view.views.TableroView;
import es.uam.eps.multij.Evento;
import es.uam.eps.multij.ExcepcionJuego;
import es.uam.eps.multij.Jugador;
import es.uam.eps.multij.JugadorAleatorio;
import es.uam.eps.multij.Partida;
import es.uam.eps.multij.PartidaListener;
import es.uam.eps.multij.Tablero;

/**
 * RoundActivity es una clase que cargará el framgento de la partida cuando la pantalla del dispositivo
 * no sea lo suficientemente grande como para contener pantalla dividida
 *
 * @author Pablo Manso
 * @version 13/03/2017
 */
public class RoundActivity extends AppCompatActivity implements PartidaListener {


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


    @BindView(R.id.board_view)
    TableroView boardView;
    @BindView(R.id.round_title)
    TextView roundTitle;
    @BindView(R.id.coordinatorRound)
    CoordinatorLayout coordinatorRound;


    /**
     * Ronda en juego
     */
    private Round round;


    /**
     * Crea todo lo necesario para la correcta ejecución de la actividad
     *
     * @param savedInstanceState Pares clave valor que nos pasa la actividad que nos invoca
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Llamamos a la clase padre
        super.onCreate(savedInstanceState);
        // Cargamos el layout de la acitividad
        setContentView(R.layout.activity_round);
        ButterKnife.bind(this);


        // Creamos una ronda y vamos metiendo uno a uno todos los argumentos
        this.round = new Round(Integer.parseInt(getIntent().getStringExtra(ARG_ROUND_SIZE)));
        this.round.setId(getIntent().getStringExtra(ARG_ROUND_ID));
        this.round.setPlayerName(getIntent().getStringExtra(ARG_FIRST_PLAYER_NAME));
        this.round.setPlayerUUID(getIntent().getStringExtra(ARG_FIRST_PLAYER_UUID));
        this.round.setTitle(getIntent().getStringExtra(ARG_ROUND_TITLE));
        this.round.setDate(getIntent().getStringExtra(ARG_ROUND_DATE));
        try {
            this.round.getBoard().stringToTablero(getIntent().getStringExtra(ARG_ROUND_BOARD));
        } catch (ExcepcionJuego excepcionJuego) {
            excepcionJuego.printStackTrace();
            finish();
        }

        roundTitle.setText(this.round.getTitle());

    }

    /**
     * Crea un intent que contiene los datos que la acitividad necesitará para ejecutarse
     *
     * @param packageContext Actividad que nos invocará
     * @param roundId        Partida que esta actividad mostrará
     * @return Intenta para invocar esta actividad
     */
    public static Intent newIntent(Context packageContext, String roundId, String playerName,
                                   String playerUUID, String roundTitle, int roundSize,
                                   String roundDate, String roundBoard) {
        // Creamos un intent entre el contexto que nos pasan y esta clase
        Intent intent = new Intent(packageContext, RoundActivity.class);
        // Adjuntamos la ronda con la clave que tenemos en la clase y devolvemos el intent
        intent.putExtra(ARG_ROUND_ID, roundId);
        intent.putExtra(ARG_FIRST_PLAYER_NAME, playerName);
        intent.putExtra(ARG_FIRST_PLAYER_UUID, playerUUID);
        intent.putExtra(ARG_ROUND_TITLE, roundTitle);
        intent.putExtra(ARG_ROUND_SIZE, Integer.toString(roundSize));
        intent.putExtra(ARG_ROUND_DATE, roundDate);
        intent.putExtra(ARG_ROUND_BOARD, roundBoard);
        return intent;
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
        startRound();
        // Llamamos al callback de la actualización y le indicamos al jugador que el cambio de ha hecho
        Snackbar.make(coordinatorRound, R.string.game_round_restarted, Snackbar.LENGTH_SHORT).show();
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
        this.boardView.setBoard(round.getBoard());
        this.boardView.setOnPlayListener(localPlayer);

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
                new AlertDialogFragment().show(getSupportFragmentManager(),"ALERT DIALOG");
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
        RoundRepository repository = RoundRepositoryFactory.createRepository(this);
        RoundRepository.BooleanCallback callback = new RoundRepository.BooleanCallback() {
            /**
             * Gestiona la respuesta del servidor a un evento de respuesta booelana
             * @param response Correcta ejecución de la función en el servidor
             */
            @Override
            public void onResponse(boolean response) {
                // Si se produce un error al actualizar la partida, se lo comunicamos al usuario
                if (!response)
                    Snackbar.make(coordinatorRound, R.string.repository_round_update_error,
                            Snackbar.LENGTH_LONG).show();
            }
        };
        // Actualizamos la partida en la base de datos
        repository.updateRound(round, callback);
    }
}