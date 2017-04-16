package es.uam.eps.dadm.activities;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import es.uam.eps.dadm.R;
import es.uam.eps.dadm.model.Round;

/**
 * RoundActivity es una clase que cargará el framgento de la partida cuando la pantalla del dispositivo
 * no sea lo suficientemente grande como para contener pantalla dividida
 * @author Pablo Manso
 * @version 13/03/2017
 */
public class RoundActivity extends AppCompatActivity  implements RoundFragment.Callbacks{

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
        // Creamos un manager para manejar el fragmento y buscamos el contenedor donde ubicaremos nuestro fragmento
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragment_container);
        // Si el fragmento devuelto es null, lo cargamos
        if (fragment == null) {
            // Creamos un gragmento directamente con el método que nos provee el fragmento
            RoundFragment roundFragment = RoundFragment.newInstance(
                    getIntent().getStringExtra(RoundFragment.ARG_ROUND_ID),
                    getIntent().getStringExtra(RoundFragment.ARG_FIRST_PLAYER_NAME),
                    getIntent().getStringExtra(RoundFragment.ARG_FIRST_PLAYER_UUID),
                    getIntent().getStringExtra(RoundFragment.ARG_ROUND_TITLE),
                    Integer.parseInt(getIntent().getStringExtra(RoundFragment.ARG_ROUND_SIZE)),
                    getIntent().getStringExtra(RoundFragment.ARG_ROUND_DATE),
                    getIntent().getStringExtra(RoundFragment.ARG_ROUND_BOARD));
            // Cargamos el gragmento a través del FragmentManager
            fm.beginTransaction()
                    .add(R.id.fragment_container, roundFragment)
                    .commit();
        }
    }

    /**
     * Crea un intent que contiene los datos que la acitividad necesitará para ejecutarse
     * @param packageContext Actividad que nos invocará
     * @param roundId Partida que esta actividad mostrará
     * @return Intenta para invocar esta actividad
     */
    public static Intent newIntent(Context packageContext, String roundId, String playerName,
                                   String playerUUID, String roundTitle, int roundSize,
                                   String roundDate, String roundBoard){
        // Creamos un intent entre el contexto que nos pasan y esta clase
        Intent intent = new Intent(packageContext, RoundActivity.class);
        // Adjuntamos la ronda con la clave que tenemos en la clase y devolvemos el intent
        intent.putExtra(RoundFragment.ARG_ROUND_ID, roundId);
        intent.putExtra(RoundFragment.ARG_FIRST_PLAYER_NAME, playerName);
        intent.putExtra(RoundFragment.ARG_FIRST_PLAYER_UUID, playerUUID);
        intent.putExtra(RoundFragment.ARG_ROUND_TITLE, roundTitle);
        intent.putExtra(RoundFragment.ARG_ROUND_SIZE, Integer.toString(roundSize));
        intent.putExtra(RoundFragment.ARG_ROUND_DATE, roundDate);
        intent.putExtra(RoundFragment.ARG_ROUND_BOARD, roundBoard);
        return intent;
    }
    @Override
    public void onRoundUpdated() {}
}