package es.uam.eps.dadm.activities;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import es.uam.eps.dadm.R;
import es.uam.eps.dadm.model.RoundRepository;
import es.uam.eps.dadm.model.RoundRepositoryFactory;

/**
 * LoginActivity muestra una actividad desde la cual el usuario podrá meter sus credenciales para
 * poder acceder al juego y recuperar sus partidas jugadas. Además el usuario se podrá registrar
 * si no tiene creada una cuenta.
 *
 * @author Pablo Manso
 * @version 12/04/2017
 */
public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    /**
     * EditText donde se escribirá el nombre de usuario
     */
    @BindView(R.id.username_edittext)
    EditText usernameEdittext;

    /**
     * EditText donde se escribirá la contraseña del usuario
     */
    @BindView(R.id.password_edittext)
    EditText passwordEdittext;

    /**
     * Referencia al repositorio donde se almacenan los datos
     */
    private RoundRepository repository;


    /**
     * Crea todo lo necesario para la correcta ejecución de la actividad
     * @param savedInstanceState Pares clave valor que nos pasa la actividad que nos invoca
     */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Cargamos el layout del login y hacemos binding de los elementos necesarios
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        // Si están almacenados los datos de algún jugador en lasp referencias...
        if (!PreferenceActivity.getPlayerName(this).equals(PreferenceActivity.PLAYERNAME_DEFAULT)) {
            // Arrancamos la actividad que muestra la lista de partidas disponibles
            startActivity(new Intent(LoginActivity.this, RoundListActivity.class));
            finish();
            return;
        }

        // Obtenemos la referencia del repositorio y comprobamos que se haya creado correctamente
        repository = RoundRepositoryFactory.createRepository(LoginActivity.this);
        if (repository == null)
            Toast.makeText(LoginActivity.this, R.string.repository_opening_error,
                    Toast.LENGTH_SHORT).show();
    }

    /**
     * Función que registra el evento click en los botones del login y ejecuta el resultado
     *
     * @param v View que invoca el método onclick
     */
    @OnClick({R.id.login_button, R.id.new_user_button, R.id.cancel_button})
    public void onClick(View v) {
        // Cogemos el nombre de usuario y la contraseña que se han introducido
        final String playername = usernameEdittext.getText().toString();
        final String password = passwordEdittext.getText().toString();

        // Creamos un callback que manejará la respuesta del servidor
        RoundRepository.LoginRegisterCallback loginRegisterCallback =
            new RoundRepository.LoginRegisterCallback() {

                /**
                 * Función que se ejecutará si el login con el repositorio se realiza de forma correcta
                 * @param playerId Identificador del usuario logueado
                 */
                @Override
                public void onLogin(String playerId) {
                    // Guardamos en las preferencias el nombre e idientificador de usuario
                    PreferenceActivity.setPlayerUUID(LoginActivity.this, playerId);
                    PreferenceActivity.setPlayerName(LoginActivity.this, playername);
                    // Arrancamos la actividad de la lista de partidas
                    startActivity(new Intent(LoginActivity.this, RoundListActivity.class));
                    finish();
                }

                /**
                 * Función que se ejecutará si el login con el repositorio devuelve en un error
                 * @param error Cadena con el error que se ha producido
                 */
                @Override
                public void onError(String error) {
                    // Se muestra el error que nos han devuelto al callback
                    Snackbar.make(findViewById(R.id.layout_login), error, Snackbar.LENGTH_SHORT).show();
                }
            };

        // Miramos cual de los tres botones se ha pulsado
        switch (v.getId()) {
            // Si es el botón login, intentamos loguearnos
            case R.id.login_button:
                repository.login(playername, password, loginRegisterCallback);
                break;
            // Si es el botón register, intentamos registrarnos
            case R.id.new_user_button:
                repository.register(playername, password, loginRegisterCallback);
                break;
            // Si es el botón cancel, finalizamos el activity
            case R.id.cancel_button:
                finish();
                break;
        }
    }


}