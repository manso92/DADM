package es.uam.eps.dadm.activities;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;

import es.uam.eps.dadm.R;
import es.uam.eps.dadm.model.Round;
import es.uam.eps.dadm.model.RoundRepository;
import es.uam.eps.dadm.model.TableroDamas;
import es.uam.eps.multij.Evento;
import es.uam.eps.multij.ExcepcionJuego;
import es.uam.eps.multij.Jugador;
import es.uam.eps.multij.JugadorAleatorio;
import es.uam.eps.multij.Partida;
import es.uam.eps.multij.PartidaListener;
import es.uam.eps.multij.Tablero;

public class RoundFragment extends Fragment implements PartidaListener{
    public static final String ARG_ROUND_ID = "es.uam.eps.dadm.round_id";
    private final int ids[][] = {
            {R.id.er1, R.id.er2, R.id.er3},
            {R.id.er4, R.id.er5, R.id.er6},
            {R.id.er7, R.id.er8, R.id.er9}};

    private int SIZE = 3;
    private int size;
    private Round round;
    private Partida game;
    private TableroDamas board;

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
            size = round.getSize();
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
        // TODO start round y no registerListeners
        //startRound();
        this.registerListeners(new JugadorHumano());
    }

    private void registerListeners(JugadorHumano local) {
        ImageButton button;
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                button = (ImageButton) this.getActivity().findViewById(ids[i][j]);
                button.setOnClickListener(local);
                button.setImageResource(R.drawable.dama_negra);
            }
        }
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
                updateUI();
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
    @Override
    public void onResume() {
        super.onResume();
        updateUI();
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

        registerListeners(localPlayer);

        if (game.getTablero().getEstado() == Tablero.EN_CURSO)
            game.comenzar();
    }

    //TODO ovveride esto por lo que tiene que intar
    private void updateUI() {
        ImageButton button;
        for (int i = 0; i < SIZE; i++)
            for (int j = 0; j < SIZE; j++) {
                /*button = (ImageButton) findViewById(ids[i][j]);
                if (board.getTablero(i, j) == ERBoard.JUGADOR1)
                    button.setBackgroundResource(R.drawable.blue_button_48dp);
                else if (board.getTablero(i, j) == ERBoard.VACIO)
                    button.setBackgroundResource(R.drawable.void_button_48dp);
                else
                    button.setBackgroundResource(R.drawable.green_button_48dp);*/
            }
    }

    @Override
    public void onCambioEnPartida(Evento evento) {
        switch (evento.getTipo()) {
            case Evento.EVENTO_CAMBIO:
                updateUI();
                break;
            case Evento.EVENTO_FIN:
                // TODO comprobar quien ha ganado la partida y en base a eso mostrarlo
                updateUI();
                callbacks.onRoundUpdated(round);
                new AlertDialogFragment().show(getActivity().getSupportFragmentManager(),
                        "ALERT DIALOG");
                break;
        }
    }
}