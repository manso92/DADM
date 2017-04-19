package es.uam.eps.dadm.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.uam.eps.dadm.R;

/**
 * HelpActivity muestra la ayuda del juego
 *
 * @author Pablo Manso
 * @version 12/02/2017
 */
public class HelpActivity extends AppCompatActivity {

    /**
     * TextView en el que se pondrá el texto de ayuda de la app
     */
    @BindView(R.id.helpTextView)
    TextView helpTextView;

    /**
     * Crea todo lo necesario para la correcta ejecución de la actividad
     * @param savedInstanceState Pares clave valor que nos pasa la actividad que nos invoca
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Cargamos el layout de la actividad y hacemos el binding de los componentes necesarios
        setContentView(R.layout.activity_help);
        ButterKnife.bind(this);

        // Cambiamos el título de la acctionbar
        getSupportActionBar().setTitle(R.string.settings_see_help);

        // Cambiamos el texto de ayuda
        helpTextView.setText(Html.fromHtml(getString(R.string.help_help_text)));
    }
}
