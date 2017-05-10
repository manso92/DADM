package es.uam.eps.dadm.view.activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.uam.eps.dadm.R;

/**
 * HelpActivity es una pantalla en que se muestra la ayuda del juego. En ella se cargará la página
 * que contiene toda la ayuda necesaria para poder utilizar la aplicación y todo lo necesario para
 * poder jugar una partida de damas
 *
 * @author Pablo Manso
 * @version 02/05/2017
 */
public class HelpActivity extends AppCompatActivity {

    /**
     * Tag para escribir en el log
     */
    public static final String DEBUG = "Damas.HelpAct";

    /**
     * Ventana emergente que mostrará el mensaje de que la página se está cargando
     */
    ProgressDialog progressDialog;

    /**
     * Webview donde se cargará la página de ayuda
     */
    @BindView(R.id.webview)
    WebView webview;

    /**
     * Prepara todos lo necesario para la correcta creación de la vista
     * @param savedInstanceState Pares clave valor que nos pasa la actividad que nos invoca
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Cargamos el layout y hacemos binding de las vistas que necesitamos
        setContentView(R.layout.activity_webview);
        ButterKnife.bind(this);

        // Cargamos la página en el webview y le ponemos el focus de la aplicación
        webview.loadUrl("file:///android_asset/help.html");
        webview.requestFocus();

        // Iniciamos el progres dialog, le colocamos las propiedades y lo mostramos
        progressDialog = new ProgressDialog(HelpActivity.this);
        progressDialog.setMessage(getString(R.string.help_loading));
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Creamos un client para manejar cuando se ha cargado la página del todo
        webview.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                // En cuanto se ha cargado la página, cerramos el dialog
                try {
                    progressDialog.dismiss();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}