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
import es.uam.eps.dadm.views.ERView;
import es.uam.eps.multij.Evento;
import es.uam.eps.multij.Jugador;
import es.uam.eps.multij.JugadorAleatorio;
import es.uam.eps.multij.Partida;
import es.uam.eps.multij.PartidaListener;
import es.uam.eps.multij.Tablero;

public class RoundFragment extends Fragment implements PartidaListener{
    public static final String ARG_ROUND_ID = "es.uam.eps.dadm.round_id";
    private Round round;
    private Partida game;
    private ERView boardView;

    private Callbacks callbacks;
    public interface Callbacks {
        void onRoundUpdated(Round round);
    }

    public RoundFragment() {
    }
    public static RoundFragment newInstance(String roundId) {
        Bundle args = new Bundle();
        args.putString(ARG_ROUND_ID, roundId);
        RoundFragment roundFragment = new RoundFragment();
        roundFragment.setArguments(args);
        return roundFragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments().containsKey(ARG_ROUND_ID)) {
            String roundId = getArguments().getString(ARG_ROUND_ID);
            round = RoundRepository.get(getActivity()).getRound(roundId);
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_round, container,
                false);
        TextView roundTitleTextView = (TextView)
                rootView.findViewById(R.id.round_title);
        roundTitleTextView.setText(round.getTitle());
        return rootView;
    }
    @Override
    public void onStart() {
        super.onStart();
        startRound();
    }

    private void registerListeners(JugadorHumano local) {
        FloatingActionButton resetButton = (FloatingActionButton)
                getView().findViewById(R.id.reset_round_fab);
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (round.getBoard().getEstado() != Tablero.EN_CURSO) {
                    Snackbar.make(getView(), R.string.round_already_finished,
                            Snackbar.LENGTH_SHORT).show();
                    return;
                }
                round.getBoard().reset();
                startRound();
                callbacks.onRoundUpdated(round);
                Snackbar.make(getView(), R.string.round_restarted,
                        Snackbar.LENGTH_SHORT).show();
            }
        });
    }
    

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        callbacks = (Callbacks) context;
    }
    @Override
    public void onDetach() {
        super.onDetach();
        callbacks = null;
    }

    void startRound() {
        ArrayList<Jugador> players = new ArrayList<Jugador>();
        JugadorAleatorio randomPlayer = new JugadorAleatorio("Random player");
        JugadorHumano localPlayer = new JugadorHumano();
        players.add(randomPlayer);
        players.add(localPlayer);
        
        game = new Partida(round.getBoard(), players);
        game.addObservador(this);
        
        localPlayer.setPartida(game);

        boardView = (ERView) getView().findViewById(R.id.board_erview);
        boardView.setBoard(round.getBoard());
        boardView.setOnPlayListener(localPlayer);

        registerListeners(localPlayer);


        if (game.getTablero().getEstado() == Tablero.EN_CURSO)
            game.comenzar();
    }

    @Override
    public void onCambioEnPartida(Evento evento) {
        switch (evento.getTipo()) {
            case Evento.EVENTO_CAMBIO:
                boardView.invalidate();
                callbacks.onRoundUpdated(round);
                break;
            case Evento.EVENTO_FIN:
                // TODO comprobar quien ha ganado la partida y en base a eso mostrarlo
                boardView.invalidate();
                callbacks.onRoundUpdated(round);
                Snackbar.make(getView(), R.string.game_over, Snackbar.LENGTH_SHORT).show();
                break;
        }
    }
}