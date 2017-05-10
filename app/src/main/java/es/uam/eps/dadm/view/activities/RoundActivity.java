package es.uam.eps.dadm.view.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.uam.eps.dadm.R;
import es.uam.eps.dadm.model.Round;
import es.uam.eps.dadm.view.fragment.RoundFragment;
import es.uam.eps.dadm.view.views.TableroView;
import es.uam.eps.multij.ExcepcionJuego;
import es.uam.eps.multij.PartidaListener;

/**
 * RoundActivity es una clase que cargará el framgento de la partida cuando la pantalla del dispositivo
 * no sea lo suficientemente grande como para contener pantalla dividida
 *
 * @author Pablo Manso
 * @version 13/03/2017
 */
public class RoundActivity extends AppCompatActivity  {

    /**
     * Tag para escribir en el log
     */
    public static final String DEBUG = "Damas.RoundAct";

    public static final String ARG_ROUND_ID = "es.uam.eps.dadm.round_id";
    public static final String ARG_FIRST_PLAYER_NAME = "es.uam.eps.dadm.first_player_name";
    public static final String ARG_FIRST_PLAYER_UUID = "es.uam.eps.dadm.first_player_uuid";
    public static final String ARG_SECOND_PLAYER_NAME = "es.uam.eps.dadm.second_player_name";
    public static final String ARG_SECOND_PLAYER_UUID = "es.uam.eps.dadm.second_player_uuid";
    public static final String ARG_ROUND_TYPE = "es.uam.eps.dadm.round_type";
    public static final String ARG_ROUND_SIZE = "es.uam.eps.dadm.round_size";
    public static final String ARG_ROUND_DATE = "es.uam.eps.dadm.round_date";
    public static final String ARG_ROUND_BOARD = "es.uam.eps.dadm.round_board";


    /**
     * Crea todo lo necesario para la correcta ejecución de la actividad
     * @param savedInstanceState Pares clave valor que nos pasa la actividad que nos invoca
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Llamamos a la clase padre
        super.onCreate(savedInstanceState);
        // Cargamos el layout de la acitividad
        setContentView(R.layout.activity_fragment);
        ButterKnife.bind(this);


        // Creamos una ronda y vamos metiendo uno a uno todos los argumentos
        // TODO revisar esto para cuando las partidas sean Online y manejar el type en el intent
        /*
      Ronda en juego
     */
        Round round = roundFromIntent();


        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragment_container);
        // Si el fragmento devuelto es null, lo cargamos
        if (fragment == null) {
            // Creamos un gragmento directamente con el método que nos provee el fragmento
            RoundFragment roundFragment = RoundFragment.newInstance(round);
            // Cargamos el gragmento a través del FragmentManager
            fm.beginTransaction()
                    .add(R.id.fragment_container, roundFragment)
                    .commit();
        }


    }

    /**
     * Crea un intent que contiene los datos que la acitividad necesitará para ejecutarse
     * @param packageContext Actividad que nos invocará
     * @param round Partida que esta actividad mostrará
     * @return Intenta para invocar esta actividad
     */
    public static Intent newIntent(Context packageContext, Round round) {
        // Creamos un intent entre el contexto que nos pasan y esta clase
        Intent intent = new Intent(packageContext, RoundActivity.class);
        // Adjuntamos la ronda con la clave que tenemos en la clase y devolvemos el intent
        intent.putExtra(ARG_ROUND_ID, round.getId());
        intent.putExtra(ARG_ROUND_TYPE, round.getTipo());
        intent.putExtra(ARG_FIRST_PLAYER_NAME, round.getFirstUserName());
        intent.putExtra(ARG_FIRST_PLAYER_UUID, round.getFirstUserUUID());
        intent.putExtra(ARG_SECOND_PLAYER_NAME, round.getSecondUserName());
        intent.putExtra(ARG_SECOND_PLAYER_UUID, round.getSecondUserUUID());
        intent.putExtra(ARG_ROUND_SIZE, Integer.toString(round.getSize()));
        intent.putExtra(ARG_ROUND_DATE, round.getDate());
        intent.putExtra(ARG_ROUND_BOARD, round.getBoard().tableroToString());
        return intent;
    }

    private Round roundFromIntent() {
        Round round = new Round(getIntent().getStringExtra(ARG_ROUND_ID),
                (Round.Type) getIntent().getSerializableExtra(ARG_ROUND_TYPE),
                getIntent().getStringExtra(ARG_ROUND_DATE),
                Integer.parseInt(getIntent().getStringExtra(ARG_ROUND_SIZE)));

        round.setFirstUser(getIntent().getStringExtra(ARG_FIRST_PLAYER_NAME),
                getIntent().getStringExtra(ARG_FIRST_PLAYER_UUID));
        round.setSecondUser(getIntent().getStringExtra(ARG_SECOND_PLAYER_NAME),
                getIntent().getStringExtra(ARG_SECOND_PLAYER_UUID));

        try {
            round.getBoard().stringToTablero(getIntent().getStringExtra(ARG_ROUND_BOARD));
        } catch (ExcepcionJuego excepcionJuego) {
            excepcionJuego.printStackTrace();
            finish();
        }
        return round;
    }









}