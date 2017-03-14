package es.uam.eps.dadm.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import es.uam.eps.dadm.R;
import es.uam.eps.dadm.model.Round;

/**
 * RoundListActivity es la actividad que contendrá la lista de partidas. Además si la pantalla es lo
 * suficientemente grande como para ser una pantalla dividida también contendrá la partida que se está jugando
 * @author Pablo Manso
 * @version 13/03/2017
 */
public class RoundListActivity extends AppCompatActivity  implements RoundListFragment.Callbacks, RoundFragment.Callbacks{

    /**
     * Crea todo lo necesario para la correcta ejecución de la actividad
     * @param savedInstanceState Pares clave valor que nos pasa la actividad que nos invoca
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Llamamos a la clase padre
        super.onCreate(savedInstanceState);
        // Cargamos el layout de la acitividad
        setContentView(R.layout.activity_masterdetail);
        // Creamos un manager para manejar el fragmento y buscamos el contenedor donde ubicaremos nuestro fragmento
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragment_container);
        // Si no se ha cargado el fragmento...
        if (fragment == null) {
            // Creamos el fragmento y lo cargamos en el contenedor
            fragment = new RoundListFragment();
            fm.beginTransaction()
                    .add(R.id.fragment_container, fragment)
                    .commit();
        }
    }

    /**
     * Función que se ejecutará cuando se dispare el listener en una partida de la lista
     * @param round Partida que el jugador ha seleccionado
     */
    @Override
    public void onRoundSelected(Round round) {
        // Si estamos en una pantalla pequeña, creamos la actividad y la arrancamos
        if (findViewById(R.id.detail_fragment_container) == null) {
            Intent intent = RoundActivity.newIntent(this, round.getId());
            startActivity(intent);
        }
        // Si estamos con la pantalla dividida, instanciamos el fragmento de la partida y la
        // cargamos en el manager de fragmentos
        else {
            RoundFragment roundFragment = RoundFragment.newInstance(round.getId());
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.detail_fragment_container, roundFragment)
                    .commit();
        }
    }

    /**
     * Función que se ejecutará cada vez que se produzca un cambio en la partida
     * @param round Partida que se ha modificado
     */
    @Override
    public void onRoundUpdated(Round round) {
        // Obtenemos el FragmentManager
        FragmentManager fragmentManager = getSupportFragmentManager();
        // Obtenemos el fragmento de la lista de partidas
        RoundListFragment roundListFragment = (RoundListFragment)
                fragmentManager.findFragmentById(R.id.fragment_container);
        // Actualizamos la lista de partidas
        roundListFragment.updateUI();
    }
}