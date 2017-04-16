package es.uam.eps.dadm.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import es.uam.eps.dadm.R;
import es.uam.eps.dadm.model.RoundRepository;
import es.uam.eps.dadm.model.RoundRepositoryFactory;

public class LoginActivity extends Activity implements View.OnClickListener {
    private RoundRepository repository;

    private EditText usernameEditText;
    private EditText passwordEditText;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        if (!PreferenceActivity.getPlayerName(this).equals(PreferenceActivity.PLAYERNAME_DEFAULT)){
            startActivity(new Intent(LoginActivity.this, RoundListActivity.class));
            finish();
            return;
        }
        usernameEditText = (EditText) findViewById(R.id.username_edittext);
        usernameEditText.setText(PreferenceActivity.PLAYERNAME_DEFAULT);
        passwordEditText = (EditText) findViewById(R.id.password_edittext);
        passwordEditText.setText(PreferenceActivity.PLAYERPASSWORD_DEFAULT);
        Button loginButton = (Button) findViewById(R.id.login_button);
        loginButton.setOnClickListener(this);
        Button cancelButton = (Button) findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(this);
        Button newUserButton = (Button) findViewById(R.id.new_user_button);
        newUserButton.setOnClickListener(this);

        repository = RoundRepositoryFactory.createRepository(LoginActivity.this);
        if (repository == null)
            Toast.makeText(LoginActivity.this, R.string.repository_opening_error,
                    Toast.LENGTH_SHORT).show();
    }

    public void onClick(View v) {
        final String playername = usernameEditText.getText().toString();
        final String password = passwordEditText.getText().toString();
        RoundRepository.LoginRegisterCallback loginRegisterCallback =
                new RoundRepository.LoginRegisterCallback() {
                    @Override
                    public void onLogin(String playerId) {
                        PreferenceActivity.setPlayerUUID(LoginActivity.this, playerId);
                        PreferenceActivity.setPlayerName(LoginActivity.this, playername);
                        PreferenceActivity.setPlayerPassword(LoginActivity.this, password);
                        startActivity(new Intent(LoginActivity.this, RoundListActivity.class));
                        finish();
                    }
                    @Override
                    public void onError(String error) {
                        // Se muestra el error que nos han devuelto al callback
                        Snackbar.make(findViewById(R.id.layout_login), error, Snackbar.LENGTH_SHORT).show();
                    }
                };
        switch (v.getId()) {
            case R.id.login_button:
                repository.login(playername, password, loginRegisterCallback);
                break;
            case R.id.cancel_button:
                finish();
                break;
            case R.id.new_user_button:
                repository.register(playername, password, loginRegisterCallback);
                break;
        }
    }


}