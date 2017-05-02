package es.uam.eps.dadm.view.activities;


import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import es.uam.eps.dadm.R;
import es.uam.eps.dadm.database.DataBase;
import es.uam.eps.dadm.model.Round;
import es.uam.eps.dadm.model.RoundRepository;
import es.uam.eps.dadm.model.RoundRepositoryFactory;

/**
 * LoginActivity es una pantalla que se muestra para que el usuario pueda hacer login con sus credenciales
 * si tiene creada una cuenta o para que registre una cuenta nueva en caso de que no tenga cuenta
 *
 * @author Pablo Manso
 * @version 02/05/2017
 */
public class LoginActivity extends AppCompatActivity {

    /**
     * EditText donde el usuario introducirá su nombre de usuario
     */
    @BindView(R.id.userEditText)
    EditText userEditText;

    /**
     * EditText donde el usuario introducirá su contraseña
     */
    @BindView(R.id.passEditText)
    EditText passEditText;

    /**
     * Progressbar que se mostrará cuando se esté haciendo login
     */
    @BindView(R.id.login_progress)
    ProgressBar loginProgress;

    /**
     * Repositorio de datos con el que manejaremos el login local
     */
    private RoundRepository localRepository;

    /**
     * Repositorio de datos con el que manejaremos el login del servidor
     */
    private RoundRepository serverRepository;

    /**
     * Prepara todos lo necesario para la correcta creación de la vista
     * @param savedInstanceState Pares clave valor que nos pasa la actividad que nos invoca
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Cargamos el layout y hacemos binding de las vistas que necesitamos
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        // Si el usuario está logueado, nos saltamos el paso del login
        if (PreferenceActivity.isLoggedIn(this)) {
            // Mostramos al usuario su lista de partidas disponibles
            // TODO Descomentar esta línea,
            //startActivity(new Intent(LoginActivity.this, RoundListActivity.class));
            //finish();
            //return;
        }

        // Arrancamos los repositorios de datos
        setupRepositories();
        // Capturamos los eventos del botón del teclado
        setupKeyboard();
    }

    /**
     * Inicia una instancia de los repositorios de datos, tanto el local como el remoto
     */
    public void setupRepositories() {
        // Comprobamos si hay conexión a internet
        if (PreferenceActivity.isOnline(this)){
            // Creamos una instancia del servidor remoto
            this.serverRepository = RoundRepositoryFactory.createRepository(this,true);
            // Si no se puede conectar se lo indicamos al usuario
            if (this.serverRepository == null)
                Toast.makeText(LoginActivity.this, R.string.repository_server_notavailable,
                        Toast.LENGTH_SHORT).show();
        } else
            // Si no hay conexión se lo indicamos al usuario
            Toast.makeText(LoginActivity.this, R.string.repository_internet_noconnection,
                    Toast.LENGTH_SHORT).show();

        // Creamos una instancia del repositorio de datos local
        this.localRepository = RoundRepositoryFactory.createRepository(this, false);
        // Si no se puede crear el repositorio local, se lo indicamos al usuario
        if (this.localRepository == null)
            Toast.makeText(LoginActivity.this, R.string.repository_database_notavailable,
                    Toast.LENGTH_SHORT).show();
    }

    /**
     * Captura los botones de acción que tiene el teclado cuando están activos los edittext
     */
    public void setupKeyboard(){
        // Capturamos el evento de teclado del EditText de usuario
        userEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                // Comprobamos que la acción es la del EditText que corresponde
                if (id == R.id.toPassword || id == EditorInfo.IME_NULL) {
                    // Pasamos el focus al campo contraseña
                    passEditText.requestFocus();
                    return true;
                }
                return false;
            }
        });

        // Capturamos el evento de teclado del EditText de la password
        passEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                // Comprobamos que la acción es la del EditText que corresponde
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    // Intentamos hacer login
                    login(findViewById(R.id.login_button));
                    return true;
                }
                return false;
            }
        });
    }

    /**
     * Captura los toques que se realicen sobre el botón de login
     * @param v View del botón que se ha pulsado
     */
    @OnClick(R.id.login_button)
    public void login(View v) {
        // Cogemos el nombre de usuario y la contraseña que se han introducido
        String user = userEditText.getText().toString();
        String pass = passEditText.getText().toString();

        // Si estamos conectados a internet el login lo hacemos con el servidor, sino lo hacemos en local
        if (PreferenceActivity.isOnline(this))
            serverLogin(user, pass);
        else
            localLogin(user, pass);
    }

    /**
     * Hace login con el repositorio del servidor
     * @param user Usuario que intenta hacer login
     * @param pass Contraseña con la que se quiere hacer login
     */
    public void serverLogin(final String user, final String pass){
        RoundRepository.LoginRegisterCallback loginCallback =
                new RoundRepository.LoginRegisterCallback() {
                    @Override
                    public void onLogin(String userUUID) {
                        ((DataBase)localRepository).register(user, pass, userUUID);
                        PreferenceActivity.setPlayerUUID(LoginActivity.this, userUUID);
                        PreferenceActivity.setPlayerName(LoginActivity.this, user);
                        // TODO descomentar esta linea
                        //startActivity(new Intent(LoginActivity.this, RoundListActivity.class));
                        finish();
                    }
                    @Override
                    public void onError(String error) {
                        Snackbar.make(findViewById(R.id.login_form), error, Snackbar.LENGTH_SHORT).show();
                    }
                };
        serverRepository.login(user, pass, loginCallback);
    }

    /**
     * Hace login con el repositorio local
     * @param user Usuario que intenta hacer login
     * @param pass Contraseña con la que se quiere hacer login
     */
    public void localLogin(final String user, final String pass){
        RoundRepository.LoginRegisterCallback loginCallback =
                new RoundRepository.LoginRegisterCallback() {
                    @Override
                    public void onLogin(String userUUID) {
                        PreferenceActivity.setPlayerUUID(LoginActivity.this, userUUID);
                        PreferenceActivity.setPlayerName(LoginActivity.this, user);
                        // TODO escribir en una preferencia que no se ha logueado online
                        // TODO descomentar esta linea
                        //startActivity(new Intent(LoginActivity.this, RoundListActivity.class));
                        finish();
                    }
                    @Override
                    public void onError(String error) {
                        Snackbar.make(findViewById(R.id.login_form), error, Snackbar.LENGTH_SHORT).show();
                    }
                };
        serverRepository.login(user, pass, loginCallback);
    }

    /**
     * Captura los toques que se realicen sobre el botón de registrar
     * @param v View del botón que se ha pulsado
     */
    @OnClick(R.id.register_button)
    public void register(View v) {
        // Cogemos el nombre de usuario y la contraseña que se han introducido
        String user = userEditText.getText().toString();
        String pass = passEditText.getText().toString();

        // Si estamos conectados a internet el login lo hacemos con el servidor, sino lo hacemos en local
        if (!PreferenceActivity.isOnline(this))
            Snackbar.make(findViewById(R.id.login_form), R.string.login_signup_error_nointernet,
                    Snackbar.LENGTH_SHORT).show();
        else
            registerUser(user, pass);
    }

    /**
     * Registra un usuario con el repositorio del servidor
     * @param user Usuario que intenta registrarse
     * @param pass Contraseña con la que se quiere registrarse
     */
    public void registerUser(final String user, final String pass){
        RoundRepository.LoginRegisterCallback loginCallback =
                new RoundRepository.LoginRegisterCallback() {
                    @Override
                    public void onLogin(String userUUID) {
                        ((DataBase)localRepository).register(user, pass, userUUID);
                        PreferenceActivity.setPlayerUUID(LoginActivity.this, userUUID);
                        PreferenceActivity.setPlayerName(LoginActivity.this, user);
                        // TODO descomentar esta linea
                        //startActivity(new Intent(LoginActivity.this, RoundListActivity.class));
                        finish();
                    }
                    @Override
                    public void onError(String error) {
                        Snackbar.make(findViewById(R.id.login_form), error, Snackbar.LENGTH_SHORT).show();
                    }
                };
        serverRepository.register(user, pass, loginCallback);
    }

    /**
     * Captura los toques que se realicen sobre el botón de login
     * @param v View del botón que se ha pulsado
     */
    @OnClick(R.id.offline_game_button)
    public void offlineGame(View v) {
        // TODO hacer que sea jugable una partida nueva
        Round round = new Round(PreferenceActivity.BOARD_SIZE_DEFAULT);

        /*Intent i = RoundActivity.newIntent(this,round.getId(),PreferenceActivity.PLAYERNAME_DEFAULT,
                PreferenceActivity.PLAYERUUID_DEFAULT, round.getTitle(), round.getSize(),
                round.getDate(), round.getBoard().tableroToString());*/
        //startActivity(i);
    }
}

