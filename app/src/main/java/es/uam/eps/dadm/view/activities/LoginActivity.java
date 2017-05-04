package es.uam.eps.dadm.view.activities;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import es.uam.eps.dadm.R;
import es.uam.eps.dadm.database.DataBase;
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
     * Layout que contiene el formulario de login
     */
    @BindView(R.id.login_form)
    ScrollView loginForm;

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
            startActivity(new Intent(LoginActivity.this, RoundListActivity.class));
            finish();
            return;
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
        if (Jarvis.isOnline(this)) {
            // Creamos una instancia del servidor remoto
            this.serverRepository = RoundRepositoryFactory.createRepository(this, true);
            // Si no se puede conectar se lo indicamos al usuario
            if (this.serverRepository == null)
                Toast.makeText(LoginActivity.this, R.string.repository_server_notavailable,
                        Toast.LENGTH_SHORT).show();
        } else
            // Si no hay conexión se lo indicamos al usuario
            Toast.makeText(LoginActivity.this, R.string.repository_server_internet_needed,
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
    public void setupKeyboard() {
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
     * Cambia la visibilidad entre el formulario de login y el loading progress
     * @param show Boolean que nos indica si se ha de ocultar el formulario (true) o no (false)
     */
    private void showProgress(final boolean show) {
        // Si es ocultar, cogemos un tiempo más largo que si es mostrar
        int time = show ? getResources().getInteger(android.R.integer.config_longAnimTime) :
                getResources().getInteger(android.R.integer.config_shortAnimTime);

        // Comenzamos a ocultar el formulario de login
        loginForm.animate().setDuration(time).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // Establecemos la visibilidad definitiva cuando la animación termine
                loginForm.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        // Comenzamos a mostrar el icono del progreso
        loginProgress.animate().setDuration(time).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // Establecemos la visibilidad definitiva cuando la animación termine
                loginProgress.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

    /**
     * Captura los toques que se realicen sobre el botón de login
     *
     * @param v View del botón que se ha pulsado
     */
    @OnClick(R.id.login_button)
    public void login(View v) {
        // Ocultamos el teclado para que la salida se vea mejor
        Jarvis.hideKeyboard(this);

        //  Comprobamos si los credenciales introducidos están entre los parámetros necesarios
        if (!comrpuebaCredenciales()) return;

        // Cogemos el nombre de usuario y la contraseña que se han introducido
        String user = userEditText.getText().toString();
        String pass = passEditText.getText().toString();

        // Ocultamos el formulario y mostramos el progress
        showProgress(true);

        // Si estamos conectados a internet el login lo hacemos con el servidor, sino lo hacemos en local
        if (Jarvis.isOnline(this))
            serverLogin(user, pass);
        else
            localLogin(user, pass);
    }

    /**
     * Hace login con el repositorio del servidor
     * @param user Usuario que intenta hacer login
     * @param pass Contraseña con la que se quiere hacer login
     */
    public void serverLogin(final String user, final String pass) {
        // Creamos un callback para cuando hagamos login con el servidor
        RoundRepository.LoginRegisterCallback loginCallback =
                new RoundRepository.LoginRegisterCallback() {
                    @Override
                    public void onLogin(String userUUID) {
                        if (!((DataBase)localRepository).existUser(user))
                            ((DataBase) localRepository).register(user, pass, userUUID, null);
                        PreferenceActivity.setPlayerUUID(LoginActivity.this, userUUID);
                        PreferenceActivity.setPlayerName(LoginActivity.this, user);
                        startActivity(new Intent(LoginActivity.this, RoundListActivity.class));
                        finish();
                    }
                    @Override
                    public void onError(String error) {
                        // Mostramos el error y volvemos a mostrar el formulario
                        Snackbar.make(findViewById(R.id.login_form), error, Snackbar.LENGTH_SHORT).show();
                        showProgress(false);
                    }
                };
        // Intentamos hacer login con el servidor
        serverRepository.login(user, pass, loginCallback);
    }

    /**
     * Hace login con el repositorio local
     * @param user Usuario que intenta hacer login
     * @param pass Contraseña con la que se quiere hacer login
     */
    public void localLogin(final String user, final String pass) {
        // Creamos un callback para cuando hagamos login con la base de datos
        RoundRepository.LoginRegisterCallback loginCallback =
                new RoundRepository.LoginRegisterCallback() {
                    @Override
                    public void onLogin(String userUUID) {
                        PreferenceActivity.setPlayerUUID(LoginActivity.this, userUUID);
                        PreferenceActivity.setPlayerName(LoginActivity.this, user);
                        startActivity(new Intent(LoginActivity.this, RoundListActivity.class));
                        finish();
                    }

                    @Override
                    public void onError(String error) {
                        // Mostramos el error y volvemos a mostrar el formulario
                        Snackbar.make(findViewById(R.id.login_form), error, Snackbar.LENGTH_SHORT).show();
                        showProgress(false);
                    }
                };
        // Intentamos hacer login con la base de datos local
        localRepository.login(user, pass, loginCallback);
    }

    /**
     * Captura los toques que se realicen sobre el botón de registrar
     * @param v View del botón que se ha pulsado
     */
    @OnClick(R.id.register_button)
    public void register(View v) {
        // Ocultamos el teclado para que la salida se vea mejor
        Jarvis.hideKeyboard(this);

        //  Comprobamos si los credenciales introducidos están entre los parámetros necesarios
        if (!comrpuebaCredenciales()) return;

        // Cogemos el nombre de usuario y la contraseña que se han introducido
        String user = userEditText.getText().toString();
        String pass = passEditText.getText().toString();

        // Si no estamos conectado, indicamos al usuario que no se puede registrar sin internet
        if (!Jarvis.isOnline(this))
            Snackbar.make(findViewById(R.id.login_form), R.string.login_signup_error_nointernet,
                    Snackbar.LENGTH_SHORT).show();
        // Ocultamos el formulario e intentamos registrar al usuario
        else {
            showProgress(true);
            registerUser(user, pass);
        }
    }

    /**
     * Registra un usuario con el repositorio del servidor
     * @param user Usuario que intenta registrarse
     * @param pass Contraseña con la que se quiere registrarse
     */
    public void registerUser(final String user, final String pass) {
        // Creamos un callback para cuando nos registremos
        RoundRepository.LoginRegisterCallback loginCallback =
                new RoundRepository.LoginRegisterCallback() {
                    @Override
                    public void onLogin(String userUUID) {
                        // Si se ha registrado con éxito en el servidor, lo registramos en local
                        ((DataBase) localRepository).register(user, pass, userUUID, null);
                        PreferenceActivity.setPlayerUUID(LoginActivity.this, userUUID);
                        PreferenceActivity.setPlayerName(LoginActivity.this, user);
                        // Mostramos las partidas disponibles para este jugador
                        startActivity(new Intent(LoginActivity.this, RoundListActivity.class));
                        finish();
                    }
                    @Override
                    public void onError(String error) {
                        // Mostramos el error y volvemos a mostrar el formulario
                        showProgress(false);
                        Snackbar.make(findViewById(R.id.login_form), error, Snackbar.LENGTH_SHORT).show();
                    }
                };

        // Intentamos registrarnos en el servidor
        serverRepository.register(user, pass, loginCallback);
    }

    /**
     * Comprueba si los parámetros introducidos de usuario y contraseña están entre los mínimos requereidos
     * En caso contrario mostrará un error en los edittext de ambos campos
     * @return Si los credenciales son válidos o no
     */
    public boolean comrpuebaCredenciales() {
        // Limpiamos los errores en caso de que hayamos mostrado un error previamente
        userEditText.setError(null);
        passEditText.setError(null);

        // Si el nombre de usuario no tiene como mínimo 6 caracteres mostramos un error
        if (userEditText.getText().toString().length() < 6)
            userEditText.setError(getString(R.string.login_username_tooshort));

        // Si la contraseña de usuario no tiene como mínimo 6 caracteres mostramos un error
        if (passEditText.getText().toString().length() < 6)
            passEditText.setError(getString(R.string.login_password_tooshort));

        // Indicamos como respuesta si se ha producido algún error
        return !((userEditText.getText().toString().length() < 6) || (passEditText.getText().toString().length() < 6));
    }

    /**
     * Captura los toques que se realicen sobre el botón de login
     *
     * @param v View del botón que se ha pulsado
     */
    @OnClick(R.id.offline_game_button)
    public void offlineGame(View v) {
        // TODO hacer que sea jugable una partida nueva
        //Round round = new Round(PreferenceActivity.BOARD_SIZE_DEFAULT);

        /*Intent i = RoundActivity.newIntent(this,round.getId(),PreferenceActivity.PLAYERNAME_DEFAULT,
                PreferenceActivity.PLAYERUUID_DEFAULT, round.getTitle(), round.getSize(),
                round.getDate(), round.getBoard().tableroToString());*/
        //startActivity(i);
    }
}

