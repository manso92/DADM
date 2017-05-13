package es.uam.eps.dadm.view.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import es.uam.eps.dadm.R;
import es.uam.eps.dadm.view.fragment.MessageFragment;

/**
 * ChatActivity es una actividad que alojará los mensajes usuario a usuario
 *
 * @author Pablo Manso
 * @version 15/05/2017
 */
public class ChatActivity extends AppCompatActivity  {

    /**
     * Tag para escribir en el log
     */
    public static final String DEBUG = "Damas.ChatAct";

    /**
     * Crea todo lo necesario para la correcta ejecución de la actividad
     * @param savedInstanceState Pares clave valor que nos pasa la actividad que nos invoca
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Cargamos el layout de la acitividad
        setContentView(R.layout.activity_fragment);


        // Cogemos el fragment manager y cargamos el fragment
        FragmentManager fm = getSupportFragmentManager();
        MessageFragment messageFragment = MessageFragment.newInstance(getIntent().getStringExtra("user"), false);
        fm.beginTransaction().add(R.id.fragment_container, messageFragment).commit();
    }

    /**
     * Crea un intent que contiene los datos que la acitividad necesitará para ejecutarse
     * @param packageContext Actividad que nos invocará
     * @param to Con quien chatearemos
     * @return Intenta para invocar esta actividad
     */
    public static Intent newIntent(Context packageContext, String to) {
        // Creamos un intent entre el contexto que nos pasan y esta clase
        Intent intent = new Intent(packageContext, ChatActivity.class);
        intent.putExtra("user", to);
        return intent;
    }

}